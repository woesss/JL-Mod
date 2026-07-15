# JL-Mod Plus crash reporter

This Cloudflare Worker replaces the retired App Center ingestion endpoint used by
JL-Mod. It accepts only user-approved ACRA crash reports, stores them in D1, and
exposes read-only admin endpoints protected by a separate token.

It does not implement installation or first-launch telemetry. The Worker does
not persist request IP addresses, request headers, or App Center installation
IDs. Reports are deleted after 90 days by default.

## Endpoints

- `GET /health` — public health check.
- `POST /v1/crash-reports` — Android ingestion endpoint; requires
  `Authorization: Bearer <INGEST_TOKEN>`.
- `GET /admin/reports?limit=50` — report summaries; requires
  `Authorization: Bearer <ADMIN_TOKEN>`.
- `GET /admin/reports/<report-id>` — full report; requires the admin token.

The ingestion token is embedded in release APKs and therefore limits accidental
or generic traffic only; it must not be treated as a user credential. The admin
token is never embedded in the app.

## Local verification

1. Copy `.dev.vars.example` to `.dev.vars` and replace both values.
2. Run `npm install`.
3. Run `npm run db:migrate:local`.
4. Run `npm test` and `npm run check`.
5. Run `npm run dev` to exercise the Worker locally.

## First deployment

1. Authenticate Wrangler with `npx wrangler login`.
2. Create the database:
   `npx wrangler d1 create jl-mod-plus-crashes`.
3. Replace the all-zero `database_id` in `wrangler.jsonc` with the returned ID.
4. Apply migrations:
   `npm run db:migrate:remote`.
5. Generate two different random values of at least 32 characters and upload
   them with `npx wrangler secret put INGEST_TOKEN` and
   `npx wrangler secret put ADMIN_TOKEN`.
6. Deploy with `npm run deploy`.
7. Add the Worker URL and the same ingestion token to the Android release
   configuration described below.

Use the free `*.workers.dev` address; a paid domain or Workers Paid plan is not
required for this service.

## Android release configuration

Add these local-only entries to the repository root `keystore.properties`:

```properties
crashReportUrl=https://jl-mod-plus-crash-reporter.h3nb-jl-mod-plus.workers.dev/v1/crash-reports
crashReportToken=<same value as INGEST_TOKEN>
fingerprint=<SHA-1 fingerprint of the release signing certificate, without colons>
```

If any value is missing or the APK signature does not match `fingerprint`, the
existing fallback writes the report to `crash.txt` instead of sending it.
