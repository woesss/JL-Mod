CREATE TABLE IF NOT EXISTS crash_reports (
    id TEXT PRIMARY KEY NOT NULL,
    received_at TEXT NOT NULL DEFAULT (datetime('now')),
    crash_at TEXT,
    app_version TEXT,
    app_build TEXT,
    app_namespace TEXT,
    os_version TEXT,
    os_api_level INTEGER,
    device_model TEXT,
    device_oem TEXT,
    exception_type TEXT,
    exception_message TEXT,
    payload_json TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_crash_reports_received_at
    ON crash_reports(received_at DESC);

CREATE INDEX IF NOT EXISTS idx_crash_reports_app_version
    ON crash_reports(app_version, received_at DESC);

CREATE INDEX IF NOT EXISTS idx_crash_reports_exception_type
    ON crash_reports(exception_type, received_at DESC);
