CREATE TABLE sales (
    id              BIGSERIAL PRIMARY KEY,
    event_id        BIGINT       NOT NULL REFERENCES events(id),
    sold_by_user_id BIGINT       NOT NULL REFERENCES users(id),
    sale_date       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_method  VARCHAR(20)  NOT NULL,
    customer_name   VARCHAR(150),
    customer_phone  VARCHAR(40),
    customer_email  VARCHAR(150),
    notes           VARCHAR(500),
    discount_amount NUMERIC(10, 2) NOT NULL DEFAULT 0,
    tax_amount      NUMERIC(10, 2) NOT NULL DEFAULT 0,
    total_amount    NUMERIC(12, 2) NOT NULL,
    is_paid         BOOLEAN      NOT NULL DEFAULT TRUE,
    is_cancelled    BOOLEAN      NOT NULL DEFAULT FALSE,
    cancelled_at    TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_sales_payment_method     CHECK (payment_method IN ('CASH','CARD','TRANSFER','OTHER')),
    CONSTRAINT chk_sales_discount_non_neg   CHECK (discount_amount >= 0),
    CONSTRAINT chk_sales_tax_non_neg        CHECK (tax_amount >= 0),
    CONSTRAINT chk_sales_total_non_neg      CHECK (total_amount >= 0)
);

CREATE INDEX idx_sales_event_id        ON sales (event_id);
CREATE INDEX idx_sales_sold_by_user_id ON sales (sold_by_user_id);
CREATE INDEX idx_sales_payment_method  ON sales (payment_method);
CREATE INDEX idx_sales_sale_date       ON sales (sale_date);
CREATE INDEX idx_sales_is_cancelled    ON sales (is_cancelled);

CREATE TABLE sale_items (
    id              BIGSERIAL PRIMARY KEY,
    sale_id         BIGINT         NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id      BIGINT         NOT NULL REFERENCES products(id),
    variant_id      BIGINT         REFERENCES product_variants(id),
    quantity        INT            NOT NULL,
    unit_price      NUMERIC(10, 2) NOT NULL,
    personalization VARCHAR(255),
    line_total      NUMERIC(12, 2) NOT NULL,
    CONSTRAINT chk_sale_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_sale_items_unit_price_non_neg CHECK (unit_price >= 0),
    CONSTRAINT chk_sale_items_line_total_non_neg CHECK (line_total >= 0)
);

CREATE INDEX idx_sale_items_sale_id    ON sale_items (sale_id);
CREATE INDEX idx_sale_items_product_id ON sale_items (product_id);
CREATE INDEX idx_sale_items_variant_id ON sale_items (variant_id);
