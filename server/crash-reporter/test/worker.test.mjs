import assert from "node:assert/strict";
import test from "node:test";

import { handleRequest, parseCrashReport } from "../src/index.mjs";

const INGEST_TOKEN = "i".repeat(48);
const ADMIN_TOKEN = "a".repeat(48);

function validPayload() {
  return {
    logs: [{
      type: "managedError",
      id: "18faeb20-fce1-4af2-b4c8-7d8779ef0a35",
      userId: "must-not-be-stored",
      timestamp: "2026-07-15T01:02:03.000Z",
      device: {
        appVersion: "0.87.1",
        appBuild: "48",
        appNamespace: "ru.woesss.j2meloader",
        osVersion: "15",
        osApiLevel: 35,
        model: "Test device",
        oemName: "Test OEM",
      },
      exception: { type: "java.lang.IllegalStateException", message: "test" },
    }],
  };
}

class FakeDatabase {
  constructor() {
    this.rows = new Map();
  }

  prepare(sql) {
    return {
      bind: (...values) => ({
        run: async () => {
          if (!sql.includes("INSERT OR IGNORE")) return { meta: { changes: 0 } };
          const id = values[0];
          if (this.rows.has(id)) return { meta: { changes: 0 } };
          this.rows.set(id, { payload_json: values.at(-1) });
          return { meta: { changes: 1 } };
        },
      }),
    };
  }
}

test("health endpoint is public", async () => {
  const response = await handleRequest(new Request("https://example.test/health"), {});
  assert.equal(response.status, 200);
  assert.equal((await response.json()).ok, true);
});

test("ingestion requires its bearer token", async () => {
  const env = { DB: new FakeDatabase(), INGEST_TOKEN, ADMIN_TOKEN };
  const response = await handleRequest(new Request("https://example.test/v1/crash-reports", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify(validPayload()),
  }), env);
  assert.equal(response.status, 401);
});

test("valid report is stored once and installation id is stripped", async () => {
  const db = new FakeDatabase();
  const env = { DB: db, INGEST_TOKEN, ADMIN_TOKEN };
  const request = () => new Request("https://example.test/v1/crash-reports", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      authorization: `Bearer ${INGEST_TOKEN}`,
    },
    body: JSON.stringify(validPayload()),
  });

  const first = await handleRequest(request(), env);
  assert.equal(first.status, 202);
  assert.equal((await first.json()).duplicate, false);
  assert.equal(db.rows.size, 1);
  assert.equal(db.rows.values().next().value.payload_json.includes("userId"), false);

  const second = await handleRequest(request(), env);
  assert.equal(second.status, 200);
  assert.equal((await second.json()).duplicate, true);
  assert.equal(db.rows.size, 1);
});

test("invalid report is rejected", () => {
  assert.deepEqual(parseCrashReport({ logs: [] }), {
    ok: false,
    error: "logs must contain between 1 and 3 entries",
  });
});

test("oversized report is rejected before parsing", async () => {
  const env = { DB: new FakeDatabase(), INGEST_TOKEN, ADMIN_TOKEN };
  const response = await handleRequest(new Request("https://example.test/v1/crash-reports", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      "content-length": String(600 * 1024),
      authorization: `Bearer ${INGEST_TOKEN}`,
    },
    body: "{}",
  }), env);
  assert.equal(response.status, 413);
});
