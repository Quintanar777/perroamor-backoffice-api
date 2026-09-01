CREATE TABLE discounts (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150)   NOT NULL,
    description     VARCHAR(500),
    total_price     NUMERIC(10, 2) NOT NULL,
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_discounts_total_non_neg CHECK (total_price >= 0)
);

CREATE INDEX idx_discounts_is_active ON discounts (is_active);

CREATE TABLE discount_slots (
    id          BIGSERIAL PRIMARY KEY,
    discount_id BIGINT      NOT NULL REFERENCES discounts(id) ON DELETE CASCADE,
    position    INT         NOT NULL,
    slot_type   VARCHAR(10) NOT NULL,
    quantity    INT         NOT NULL,
    CONSTRAINT chk_slot_type              CHECK (slot_type IN ('FIXED', 'GROUP')),
    CONSTRAINT chk_slot_qty               CHECK (quantity > 0),
    CONSTRAINT chk_group_slot_single_unit CHECK (slot_type <> 'GROUP' OR quantity = 1),
    CONSTRAINT uq_slot_position UNIQUE (discount_id, position)
);

CREATE INDEX idx_discount_slots_discount_id ON discount_slots (discount_id);

CREATE TABLE discount_slot_options (
    id                BIGSERIAL PRIMARY KEY,
    slot_id           BIGINT         NOT NULL REFERENCES discount_slots(id) ON DELETE CASCADE,
    product_id        BIGINT         NOT NULL REFERENCES products(id),
    final_unit_price  NUMERIC(10, 2) NOT NULL,
    CONSTRAINT chk_option_price_non_neg CHECK (final_unit_price >= 0),
    CONSTRAINT uq_slot_product UNIQUE (slot_id, product_id)
);

CREATE INDEX idx_discount_slot_options_slot_id    ON discount_slot_options (slot_id);
CREATE INDEX idx_discount_slot_options_product_id ON discount_slot_options (product_id);

ALTER TABLE sale_items
    ADD COLUMN discount_id BIGINT REFERENCES discounts(id);

CREATE INDEX idx_sale_items_discount_id ON sale_items (discount_id);
