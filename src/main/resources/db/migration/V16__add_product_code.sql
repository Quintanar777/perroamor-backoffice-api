ALTER TABLE products ADD COLUMN code VARCHAR(60);

CREATE UNIQUE INDEX idx_products_code ON products (code) WHERE code IS NOT NULL;
