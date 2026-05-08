-- Productos de la marca "Perra Madre". Wholesale = 85% del retail.

INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Premio Natural Pollo',     b.id, 'Premios', 85.00, 72.25, 20, 'Premios naturales de pollo deshidratado para entrenamientos', FALSE, FALSE FROM brands b WHERE b.name = 'Perra Madre';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Snack Dental',             b.id, 'Premios', 65.00, 55.25, 15, 'Snacks dentales para la higiene bucal de tu mascota',           FALSE, FALSE FROM brands b WHERE b.name = 'Perra Madre';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Huesos de Cuero Natural',  b.id, 'Premios', 45.00, 38.25, 25, 'Huesos de cuero 100% natural para masticación',                FALSE, TRUE  FROM brands b WHERE b.name = 'Perra Madre';

-- Variantes: Huesos de Cuero Natural
INSERT INTO product_variants (product_id, variant_name, size, stock, sku)
SELECT p.id, 'Pequeño', 'S', 10, 'HCN-S' FROM products p WHERE p.name = 'Huesos de Cuero Natural';
INSERT INTO product_variants (product_id, variant_name, size, stock, sku)
SELECT p.id, 'Mediano', 'M',  8, 'HCN-M' FROM products p WHERE p.name = 'Huesos de Cuero Natural';
INSERT INTO product_variants (product_id, variant_name, size, stock, sku)
SELECT p.id, 'Grande',  'L',  7, 'HCN-L' FROM products p WHERE p.name = 'Huesos de Cuero Natural';
