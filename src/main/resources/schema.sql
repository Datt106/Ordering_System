-- SQLite schema — Import Ordering System (ITSS)

CREATE TABLE IF NOT EXISTS users (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    username        TEXT NOT NULL UNIQUE,
    password_hash   TEXT NOT NULL,
    role            TEXT NOT NULL,
    site_code       TEXT
);

CREATE TABLE IF NOT EXISTS sites (
    site_code             TEXT PRIMARY KEY,
    site_name             TEXT NOT NULL,
    ship_days             INTEGER,
    air_days              INTEGER,
    other_info            TEXT,
    active                INTEGER NOT NULL DEFAULT 1,
    shipping_status       TEXT NOT NULL DEFAULT 'CHUA_KHAI_BAO',
    shipping_updated_at   TEXT
);

CREATE TABLE IF NOT EXISTS standard_merchandise (
    merchandise_code   TEXT PRIMARY KEY,
    merchandise_name   TEXT,
    description        TEXT
);

CREATE TABLE IF NOT EXISTS site_merchandise (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    site_code         TEXT NOT NULL REFERENCES sites (site_code),
    merchandise_code  TEXT NOT NULL,
    updated_at        TEXT,
    UNIQUE (site_code, merchandise_code)
);

CREATE TABLE IF NOT EXISTS import_requests (
    request_id    TEXT PRIMARY KEY,
    created_at    TEXT NOT NULL,
    created_by    TEXT NOT NULL,
    department    TEXT NOT NULL,
    status        TEXT NOT NULL,
    processed_by  TEXT,
    processed_at  TEXT
);

CREATE TABLE IF NOT EXISTS import_request_items (
    id                    INTEGER PRIMARY KEY AUTOINCREMENT,
    request_id            TEXT NOT NULL REFERENCES import_requests (request_id) ON DELETE CASCADE,
    merchandise_code      TEXT NOT NULL,
    quantity_ordered      INTEGER NOT NULL,
    unit                  TEXT NOT NULL,
    desired_delivery_date TEXT NOT NULL,
    item_status           TEXT NOT NULL DEFAULT 'OK'
);

CREATE TABLE IF NOT EXISTS inventory_queries (
    query_id           TEXT PRIMARY KEY,
    request_id         TEXT NOT NULL,
    site_code          TEXT NOT NULL,
    merchandise_code   TEXT NOT NULL,
    in_stock_quantity  INTEGER NOT NULL DEFAULT 0,
    unit               TEXT NOT NULL,
    responded_at       TEXT
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    order_id          TEXT PRIMARY KEY,
    request_id        TEXT NOT NULL,
    site_code         TEXT NOT NULL,
    merchandise_code  TEXT NOT NULL,
    quantity_ordered  INTEGER NOT NULL,
    unit              TEXT NOT NULL,
    delivery_means    TEXT NOT NULL,
    status            TEXT NOT NULL,
    sent_at           TEXT,
    confirmed_at      TEXT,
    actual_quantity   INTEGER,
    quantity_diff     INTEGER
);

CREATE INDEX IF NOT EXISTS idx_import_request_items_request ON import_request_items (request_id);
CREATE INDEX IF NOT EXISTS idx_inventory_queries_request ON inventory_queries (request_id);
CREATE INDEX IF NOT EXISTS idx_inventory_queries_site_pending ON inventory_queries (site_code, responded_at);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_request ON purchase_orders (request_id);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_site ON purchase_orders (site_code);
