const MAX_BODY_BYTES = 512 * 1024;
const DEFAULT_RETENTION_DAYS = 90;
const MAX_RETENTION_DAYS = 365;

const JSON_HEADERS = {
  "content-type": "application/json; charset=utf-8",
  "cache-control": "no-store",
  "x-content-type-options": "nosniff",
};

export default {
  fetch(request, env) {
    return handleRequest(request, env);
  },

  scheduled(_controller, env, ctx) {
    ctx.waitUntil(deleteExpiredReports(env));
  },
};

export async function handleRequest(request, env) {
  const url = new URL(request.url);

  if (request.method === "GET" && url.pathname === "/health") {
    return json({ ok: true, service: "jl-mod-plus-crash-reporter" });
  }

  if (request.method === "POST" && url.pathname === "/v1/crash-reports") {
    return ingestCrashReport(request, env);
  }

  if (request.method === "GET" && url.pathname === "/admin/reports") {
    return listReports(request, env);
  }

  if (request.method === "GET" && url.pathname.startsWith("/admin/reports/")) {
    return getReport(request, env, decodeURIComponent(url.pathname.slice(15)));
  }

  return json({ error: "not_found" }, 404);
}

async function ingestCrashReport(request, env) {
  if (!hasBearerToken(request, env.INGEST_TOKEN)) {
    return json({ error: "unauthorized" }, 401);
  }

  const contentType = request.headers.get("content-type") || "";
  if (!contentType.toLowerCase().includes("application/json")) {
    return json({ error: "content_type_must_be_json" }, 415);
  }

  const declaredLength = Number(request.headers.get("content-length") || 0);
  if (declaredLength > MAX_BODY_BYTES) {
    return json({ error: "report_too_large" }, 413);
  }

  const body = await request.arrayBuffer();
  if (body.byteLength === 0) {
    return json({ error: "empty_report" }, 400);
  }
  if (body.byteLength > MAX_BODY_BYTES) {
    return json({ error: "report_too_large" }, 413);
  }

  let payload;
  try {
    payload = JSON.parse(new TextDecoder().decode(body));
  } catch {
    return json({ error: "invalid_json" }, 400);
  }

  const parsed = parseCrashReport(payload);
  if (!parsed.ok) {
    return json({ error: "invalid_report", detail: parsed.error }, 400);
  }

  // Older clients may include App Center's installation identifier. It is not
  // needed by this service and is stripped before the payload is persisted.
  stripUserIds(payload);
  const payloadJson = JSON.stringify(payload);

  const result = await env.DB.prepare(`
    INSERT OR IGNORE INTO crash_reports (
      id, crash_at, app_version, app_build, app_namespace,
      os_version, os_api_level, device_model, device_oem,
      exception_type, exception_message, payload_json
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `).bind(
    parsed.id,
    parsed.crashAt,
    parsed.device.appVersion,
    parsed.device.appBuild,
    parsed.device.appNamespace,
    parsed.device.osVersion,
    parsed.device.osApiLevel,
    parsed.device.model,
    parsed.device.oemName,
    parsed.exception.type,
    parsed.exception.message,
    payloadJson,
  ).run();

  const inserted = Number(result.meta?.changes || 0) > 0;
  return json({ accepted: true, duplicate: !inserted, id: parsed.id }, inserted ? 202 : 200);
}

async function listReports(request, env) {
  if (!hasBearerToken(request, env.ADMIN_TOKEN)) {
    return json({ error: "unauthorized" }, 401);
  }

  const url = new URL(request.url);
  const requestedLimit = Number.parseInt(url.searchParams.get("limit") || "50", 10);
  const limit = Number.isFinite(requestedLimit) ? Math.min(Math.max(requestedLimit, 1), 100) : 50;
  const before = url.searchParams.get("before") || "9999-12-31T23:59:59Z";

  const result = await env.DB.prepare(`
    SELECT id, received_at, crash_at, app_version, app_build, app_namespace,
           os_version, os_api_level, device_model, device_oem,
           exception_type, exception_message
      FROM crash_reports
     WHERE received_at < ?
     ORDER BY received_at DESC
     LIMIT ?
  `).bind(before, limit).all();

  return json({ reports: result.results || [], count: (result.results || []).length });
}

async function getReport(request, env, id) {
  if (!hasBearerToken(request, env.ADMIN_TOKEN)) {
    return json({ error: "unauthorized" }, 401);
  }
  if (!id) {
    return json({ error: "not_found" }, 404);
  }

  const row = await env.DB.prepare(`
    SELECT id, received_at, crash_at, app_version, app_build, app_namespace,
           os_version, os_api_level, device_model, device_oem,
           exception_type, exception_message, payload_json
      FROM crash_reports
     WHERE id = ?
  `).bind(id).first();

  if (!row) {
    return json({ error: "not_found" }, 404);
  }

  let payload = null;
  try {
    payload = JSON.parse(row.payload_json);
  } catch {
    payload = row.payload_json;
  }
  delete row.payload_json;
  return json({ report: row, payload });
}

export async function deleteExpiredReports(env) {
  const configured = Number.parseInt(env.RETENTION_DAYS || "", 10);
  const days = Number.isFinite(configured)
    ? Math.min(Math.max(configured, 1), MAX_RETENTION_DAYS)
    : DEFAULT_RETENTION_DAYS;
  return env.DB.prepare(
    "DELETE FROM crash_reports WHERE received_at < datetime('now', ?)",
  ).bind(`-${days} days`).run();
}

export function parseCrashReport(payload) {
  if (!payload || typeof payload !== "object" || !Array.isArray(payload.logs)) {
    return { ok: false, error: "logs must be an array" };
  }
  if (payload.logs.length < 1 || payload.logs.length > 3) {
    return { ok: false, error: "logs must contain between 1 and 3 entries" };
  }

  const error = payload.logs.find((entry) => entry?.type === "managedError");
  if (!error || typeof error.id !== "string" || !isUuid(error.id)) {
    return { ok: false, error: "managedError.id must be a UUID" };
  }
  if (!error.device || typeof error.device !== "object") {
    return { ok: false, error: "managedError.device is required" };
  }
  if (!error.exception || typeof error.exception !== "object") {
    return { ok: false, error: "managedError.exception is required" };
  }

  return {
    ok: true,
    id: error.id,
    crashAt: textOrNull(error.timestamp),
    device: {
      appVersion: textOrNull(error.device.appVersion),
      appBuild: textOrNull(error.device.appBuild),
      appNamespace: textOrNull(error.device.appNamespace),
      osVersion: textOrNull(error.device.osVersion),
      osApiLevel: integerOrNull(error.device.osApiLevel),
      model: textOrNull(error.device.model),
      oemName: textOrNull(error.device.oemName),
    },
    exception: {
      type: textOrNull(error.exception.type),
      message: textOrNull(error.exception.message),
    },
  };
}

function stripUserIds(value) {
  if (!value || typeof value !== "object") return;
  if (Array.isArray(value)) {
    value.forEach(stripUserIds);
    return;
  }
  delete value.userId;
  for (const child of Object.values(value)) stripUserIds(child);
}

function hasBearerToken(request, expected) {
  if (typeof expected !== "string" || expected.length < 32) return false;
  const supplied = request.headers.get("authorization") || "";
  return supplied === `Bearer ${expected}`;
}

function isUuid(value) {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value);
}

function textOrNull(value) {
  return typeof value === "string" && value.length > 0 ? value.slice(0, 1024) : null;
}

function integerOrNull(value) {
  return Number.isInteger(value) ? value : null;
}

function json(value, status = 200) {
  return new Response(JSON.stringify(value), { status, headers: JSON_HEADERS });
}
