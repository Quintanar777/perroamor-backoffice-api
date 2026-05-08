CREATE TABLE brands (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_brands_is_active ON brands (is_active);

CREATE TABLE products (
    id                   BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(150)   NOT NULL,
    brand_id             BIGINT         NOT NULL REFERENCES brands(id),
    category             VARCHAR(80)    NOT NULL,
    price                NUMERIC(10, 2) NOT NULL,
    wholesale_price      NUMERIC(10, 2) NOT NULL,
    stock                INT            NOT NULL DEFAULT 0,
    description          VARCHAR(1000),
    can_be_personalized  BOOLEAN        NOT NULL DEFAULT FALSE,
    has_variants         BOOLEAN        NOT NULL DEFAULT FALSE,
    is_active            BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_products_price_non_negative          CHECK (price >= 0),
    CONSTRAINT chk_products_wholesale_price_non_neg     CHECK (wholesale_price >= 0),
    CONSTRAINT chk_products_stock_non_negative          CHECK (stock >= 0)
);

CREATE INDEX idx_products_brand_id   ON products (brand_id);
CREATE INDEX idx_products_category   ON products (category);
CREATE INDEX idx_products_is_active  ON products (is_active);

CREATE TABLE product_variants (
    id                BIGSERIAL PRIMARY KEY,
    product_id        BIGINT         NOT NULL REFERENCES products(id),
    variant_name      VARCHAR(150)   NOT NULL,
    color             VARCHAR(60),
    size              VARCHAR(40),
    design            VARCHAR(120),
    material          VARCHAR(120),
    sku               VARCHAR(60)    NOT NULL UNIQUE,
    stock             INT            NOT NULL DEFAULT 0,
    price_adjustment  NUMERIC(10, 2) NOT NULL DEFAULT 0,
    is_active         BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_variants_stock_non_negative CHECK (stock >= 0)
);

CREATE INDEX idx_variants_product_id ON product_variants (product_id);
CREATE INDEX idx_variants_is_active  ON product_variants (is_active);
