-- Productos de la marca "Perro Amor". Wholesale = 85% del retail (convención migrada de la app vieja).

INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Santo Remedio',  b.id, 'Collares',        199.00, 169.15,   0, 'Collar elegante con diseño único',                            TRUE,  TRUE  FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Correa Binomio',        b.id, 'Correas',         189.00, 160.65,   0, 'Correa resistente con diseños únicos',                       FALSE, TRUE  FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Porta Alerta',          b.id, 'Correas',         139.00, 118.15,   0, 'Correa de seguridad con alta visibilidad',                   FALSE, TRUE  FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vida Mía',       b.id, 'Collares',        199.00, 169.15,  10, 'Collar con estilo único para tu mascota',                    TRUE,  FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Mochila Mimi',          b.id, 'Mochilas',        289.00, 245.65,   5, 'Mochila práctica para paseos largos',                        FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Grabado de Nombre',     b.id, 'Personalización', 50.00,  42.50, 100, 'Servicio de grabado de nombre en productos',                 FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Grabado de Teléfono',   b.id, 'Personalización', 50.00,  42.50, 100, 'Servicio de grabado de teléfono en productos',               FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';

-- Variantes: Collar Santo Remedio
INSERT INTO product_variants (product_id, variant_name, color, size, material, stock, sku)
SELECT p.id, 'Listón Rojo - S',  'Rojo',  'S', 'Listón 1.9cm', 15, 'CSR-R-S' FROM products p WHERE p.name = 'Collar Santo Remedio';
INSERT INTO product_variants (product_id, variant_name, color, size, material, stock, sku)
SELECT p.id, 'Listón Azul - M',  'Azul',  'M', 'Listón 3cm',   12, 'CSR-A-M' FROM products p WHERE p.name = 'Collar Santo Remedio';
INSERT INTO product_variants (product_id, variant_name, color, size, material, stock, sku)
SELECT p.id, 'Listón Verde - L', 'Verde', 'L', 'Listón 3cm',    8, 'CSR-V-L' FROM products p WHERE p.name = 'Collar Santo Remedio';

-- Variantes: Correa Binomio
INSERT INTO product_variants (product_id, variant_name, design,         color,         size,    stock, sku)
SELECT p.id, 'Pilatos Dog - M',     'Pilatos Dog',  'Multicolor',   'M',     8, 'CB-PD-M'  FROM products p WHERE p.name = 'Correa Binomio';
INSERT INTO product_variants (product_id, variant_name, design,         color,         size,    stock, sku)
SELECT p.id, 'Dolce Vida - L',      'Dolce Vida',   'Rosa/Dorado',  'L',     6, 'CB-DV-L'  FROM products p WHERE p.name = 'Correa Binomio';
INSERT INTO product_variants (product_id, variant_name, design,         color,         size,    stock, sku)
SELECT p.id, 'Ohana - S',           'Ohana',        'Azul/Blanco',  'S',     4, 'CB-OH-S'  FROM products p WHERE p.name = 'Correa Binomio';
INSERT INTO product_variants (product_id, variant_name, design,         color,         size,    stock, sku)
SELECT p.id, 'Love is Love - Único','Love is Love', 'Arcoíris',     'Único', 3, 'CB-LIL-U' FROM products p WHERE p.name = 'Correa Binomio';

-- Variantes: Porta Alerta
INSERT INTO product_variants (product_id, variant_name, color,    size,       stock, sku)
SELECT p.id, 'Rojo - S',         'Rojo',     'S',        10, 'PA-R-S'  FROM products p WHERE p.name = 'Porta Alerta';
INSERT INTO product_variants (product_id, variant_name, color,    size,       stock, sku)
SELECT p.id, 'Morado - M',       'Morado',   'M',         8, 'PA-M-M'  FROM products p WHERE p.name = 'Porta Alerta';
INSERT INTO product_variants (product_id, variant_name, color,    size,       stock, sku)
SELECT p.id, 'Rosa - L',         'Rosa',     'L',        12, 'PA-RS-L' FROM products p WHERE p.name = 'Porta Alerta';
INSERT INTO product_variants (product_id, variant_name, color,    size,       stock, sku)
SELECT p.id, 'Naranja - Mediano','Naranja',  'Mediano',   6, 'PA-N-MD' FROM products p WHERE p.name = 'Porta Alerta';
INSERT INTO product_variants (product_id, variant_name, color,    size,       stock, sku)
SELECT p.id, 'Amarillo - Grande','Amarillo', 'Grande',    9, 'PA-A-G'  FROM products p WHERE p.name = 'Porta Alerta';
INSERT INTO product_variants (product_id, variant_name, color,    size,       stock, sku)
SELECT p.id, 'Verde - Unitalla', 'Verde',    'Unitalla',  7, 'PA-V-U'  FROM products p WHERE p.name = 'Porta Alerta';
INSERT INTO product_variants (product_id, variant_name, color,    size,       stock, sku)
SELECT p.id, 'Azul - XL',        'Azul',     'XL',       11, 'PA-AZ-XL'FROM products p WHERE p.name = 'Porta Alerta';
