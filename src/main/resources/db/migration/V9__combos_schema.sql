CREATE TABLE combos (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150)   NOT NULL,
    description     VARCHAR(1000),
    brand_id        BIGINT         NOT NULL REFERENCES brands(id),
    price           NUMERIC(10, 2) NOT NULL,
    wholesale_price NUMERIC(10, 2) NOT NULL,
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_combos_price_non_neg           CHECK (price >= 0),
    CONSTRAINT chk_combos_wholesale_price_non_neg CHECK (wholesale_price >= 0)
);

CREATE INDEX idx_combos_brand_id  ON combos (brand_id);
CREATE INDEX idx_combos_is_active ON combos (is_active);

CREATE TABLE combo_items (
    id          BIGSERIAL PRIMARY KEY,
    combo_id    BIGINT NOT NULL REFERENCES combos(id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES products(id),
    variant_id  BIGINT          REFERENCES product_variants(id),
    quantity    INT    NOT NULL,
    CONSTRAINT chk_combo_items_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_combo_items_combo_id   ON combo_items (combo_id);
CREATE INDEX idx_combo_items_product_id ON combo_items (product_id);
CREATE INDEX idx_combo_items_variant_id ON combo_items (variant_id);

ALTER TABLE sale_items
    ADD COLUMN combo_id BIGINT REFERENCES combos(id);

CREATE INDEX idx_sale_items_combo_id ON sale_items (combo_id);

-- Cuando un sale_item representa un combo: combo_id NOT NULL, product_id y variant_id NULL.
-- Cuando es un producto suelto: combo_id NULL, product_id NOT NULL.
-- Hacemos product_id nullable para soportar el caso "combo".
ALTER TABLE sale_items
    ALTER COLUMN product_id DROP NOT NULL;

ALTER TABLE sale_items
    ADD CONSTRAINT chk_sale_items_combo_or_product
    CHECK (
        (combo_id IS NOT NULL AND product_id IS NULL  AND variant_id IS NULL)
     OR (combo_id IS NULL     AND product_id IS NOT NULL)
    );
