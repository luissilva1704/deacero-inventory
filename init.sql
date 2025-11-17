--Creación de usuario y permisos
CREATE ROLE deacerouser1 WITH LOGIN PASSWORD 'Password123#';
GRANT ALL PRIVILEGES ON DATABASE deacero_inventory_db TO deacerouser1;
GRANT CONNECT ON DATABASE "deacero_inventory_db" TO deacerouser1;


--Creación de tablas
CREATE EXTENSION IF NOT EXISTS pgcrypto;


DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'transaction_type') THEN
        CREATE TYPE transaction_type AS ENUM ('IN', 'OUT', 'TRANSFER');
    END IF;
END$$;


CREATE TABLE IF NOT EXISTS product (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    category    VARCHAR(100),
    price       NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    sku         VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS inventory (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    store_id   VARCHAR(50) NOT NULL,
    quantity   INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    min_stock  INTEGER NOT NULL DEFAULT 0 CHECK (min_stock >= 0),

    CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE INDEX IF NOT EXISTS idx_inventory_product_store
    ON inventory (product_id, store_id);

CREATE TABLE IF NOT EXISTS transaction (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID NOT NULL,
    source_store_id VARCHAR(50),
    target_store_id VARCHAR(50),
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    "timestamp"     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    type            transaction_type NOT NULL,

    CONSTRAINT fk_movement_product
        FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE INDEX IF NOT EXISTS idx_movement_product
    ON transaction (product_id);

CREATE INDEX IF NOT EXISTS idx_movement_timestamp
    ON transaction ("timestamp");


-- Dar acceso a todas las tablas actuales
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO deacerouser1;

