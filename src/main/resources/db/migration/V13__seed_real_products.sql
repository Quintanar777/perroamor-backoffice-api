-- Datos reales de catálogo. Fuente: products_202605111721.sql
-- Excluidos: Kit*, Combo*, "FAKE para pruebas". Stock inicial en 0 para todos.

-- ── Marcas ────────────────────────────────────────────────────────────────────
INSERT INTO brands (name, description, is_active) VALUES
    ('Perro Amor',  'Marca principal: collares, correas, mochilas, personalización.', TRUE),
    ('Perra Madre', 'Línea de premios y snacks naturales para mascotas.',              TRUE),
    ('Pashminas',   'Línea de ropa y accesorios textiles para mascotas.',              TRUE);

-- ── Productos Perro Amor ──────────────────────────────────────────────────────
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Correa',                      b.id, 'Correas',         189.00,  99.00,  0, 'Correa',                           FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Partner',                     b.id, 'Correas',         189.00, 109.00,  0, 'Partner',                          FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Correa Alerta (Simbolo)',      b.id, 'Correas',         169.00, 119.00,  0, 'Correa Alerta',                    FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Correa Resorte',              b.id, 'Correas',         199.00, 169.15,  0, NULL,                               FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';

INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Alebrije',                    b.id, 'Collares',        219.00, 139.00,  0, 'Alebrije',                         FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Vanny',                       b.id, 'Collares',        169.00,  99.00,  0, 'Collar Vanny',                     FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Lacito',                      b.id, 'Collares',        199.00, 119.00,  0, 'Lacito',                           FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'San Michito',                 b.id, 'Collares',        110.00,  79.00,  0, 'San Michito',                      FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Royal "L / M"',               b.id, 'Collares',        319.00, 259.00,  0, 'Royal "L / M"',                    FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Royal "XS / S"',              b.id, 'Collares',        299.00, 239.00,  0, 'Collar Royal',                     FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Vida Mia / Milagro "L / M"',  b.id, 'Collares',        169.00,  89.00,  0, 'Vida Mia / Milagro "L / M"',       FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Vida Mia / Milagro "S"',      b.id, 'Collares',        149.00,  89.00,  0, 'Vida Mia / Milagro Chico "S"',     FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Oferta 99',                   b.id, 'Collares',         99.00,  84.15,  0, NULL,                               FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Oferta 150',                  b.id, 'Collares',        150.00, 127.50,  0, NULL,                               FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';

INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Mochila Normal',              b.id, 'Mochilas',        259.00, 175.00,  0, 'Mochila Normal',                   FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Mochila Sticker',             b.id, 'Mochilas',        319.00, 205.00,  0, 'Mochila Sticker',                  FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';

INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Bolsita',                     b.id, 'Accesorios',       79.00,  40.00,  0, 'Bolsita',                          FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Sereno Moreno',               b.id, 'Accesorios',      340.00, 239.00,  0, 'Arnes Sereno Moreno',              FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera Basica',              b.id, 'Accesorios',      130.00,  89.00,  0, 'Pechera Basica',                   FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera "XS / S"',            b.id, 'Accesorios',      309.00, 189.00,  0, 'Pechera "XS / S"',                 FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera "M / L"',             b.id, 'Accesorios',      329.00, 189.00,  0, 'Pechera "M / L"',                  FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Porta Alerta',                b.id, 'Accesorios',      169.00, 143.65,  0, 'Porta Alerta',                     FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';

INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Juguete',                     b.id, 'Juguetes',         99.00,  99.00,  0, 'Juguete',                          FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';

INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Letras',                      b.id, 'Personalización',   5.00,   4.25,  0, NULL,                               FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';

-- ── Productos Perra Madre ─────────────────────────────────────────────────────
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Boqueron',                    b.id, 'Accesorios',      150.00, 127.50,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perra Madre';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Salmon',                      b.id, 'Accesorios',      150.00, 127.50,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perra Madre';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Charal',                      b.id, 'Accesorios',       90.00,  76.50,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perra Madre';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Tiras de cerdo',              b.id, 'Accesorios',      110.00,  93.50,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perra Madre';

-- ── Productos Pashminas ───────────────────────────────────────────────────────
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pashmina Mediana',            b.id, 'Accesorios',      210.00, 178.50,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pashminas Chica',             b.id, 'Accesorios',      199.00, 169.15,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pasminas Grande',             b.id, 'Accesorios',      230.00, 195.50,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pashminas XGrande',           b.id, 'Accesorios',      250.00, 212.50,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Sudadera XChica',             b.id, 'Accesorios',      319.00, 271.15,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Sudadera Chica',              b.id, 'Accesorios',      349.00, 296.65,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Sudadera Mediana',            b.id, 'Accesorios',      379.00, 322.15,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Sudadera Grande',             b.id, 'Accesorios',      409.00, 347.65,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Sudadera XGrande',            b.id, 'Accesorios',      459.00, 390.15,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Sud Forro XChica',            b.id, 'Accesorios',      519.00, 441.15,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Sud Forro Chica',             b.id, 'Accesorios',      589.00, 500.65,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Sud Forro Mediana',           b.id, 'Accesorios',      659.00, 560.15,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Sub Forro Grande',            b.id, 'Accesorios',      729.00, 619.65,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Sud Forro XGrande',           b.id, 'Accesorios',      799.00, 679.15,  0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Pashminas';
