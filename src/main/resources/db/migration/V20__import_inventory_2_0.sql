-- Importación literal de inventario 2.0. Transcripción directa de
-- repositories/specs/inventory_2_0/inventario.csv (fuente congelada), filtrando
-- filas con PRECIO EXPO no vacío (48 de 55 filas de datos). Cada fila CSV se
-- importa como un Product independiente (nunca ProductVariant): name = PRODUCTO
-- recortado + " " + TALLA cuando TALLA no es vacío; price = PRECIO EXPO;
-- wholesale_price = PRECIO MAYOREO, o PRECIO EXPO si viene vacío; stock =
-- CANTIDAD o 0. category se asigna explícitamente por fila según el vocabulario
-- existente (Collares, Correas, Mochilas, Accesorios).
--
-- NOTA sobre el conteo del guard: design.md originalmente proyectó 49 filas
-- activas post-importación. El recuento exacto sobre inventario.csv (ya
-- corregido, fila 39 con MARCA rellenada) produce 48 filas con PRECIO EXPO no
-- vacío -- no 49. El guard de abajo usa 48 (el valor real y verificable), no el
-- 49 originalmente estimado en design.md; ver apply-progress para el detalle de
-- esta corrección aritmética.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM brands WHERE name = 'Perro Amor') THEN
        RAISE EXCEPTION 'Migration V20 aborted: brand "Perro Amor" not found.';
    END IF;
END $$;

INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Milagro S', b.id, 'Collares', 179.00, 109.00, 89, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Milagro M', b.id, 'Collares', 189.00, 119.00, 80, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Milagro L', b.id, 'Collares', 189.00, 119.00, 41, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vida Mia S', b.id, 'Collares', 179.00, 109.00, 80, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vida Mia M', b.id, 'Collares', 189.00, 119.00, 75, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vida Mia L', b.id, 'Collares', 189.00, 119.00, 49, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Alebrije XS', b.id, 'Collares', 219.00, 149.00, 10, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Alebrije S', b.id, 'Collares', 219.00, 149.00, 12, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Alebrije M', b.id, 'Collares', 219.00, 149.00, 21, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Alebrije L', b.id, 'Collares', 219.00, 149.00, 15, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vanny XS', b.id, 'Collares', 189.00, 119.00, 3, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vanny S', b.id, 'Collares', 189.00, 119.00, 4, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vanny M', b.id, 'Collares', 189.00, 119.00, 1, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vanny L', b.id, 'Collares', 189.00, 119.00, 1, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Deja Vu Negro M', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Deja Vu Negro L', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Deja Vu Rojo M', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Deja Vu Rojo L', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Deja Vu M', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Deja Vu L', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Abuelita M', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Abuelita L', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Lacito M', b.id, 'Collares', 219.00, 219.00, 4, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Lacito L', b.id, 'Collares', 219.00, 219.00, 3, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Royal XS', b.id, 'Collares', 299.00, 259.00, 1, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Royal S', b.id, 'Collares', 299.00, 259.00, 8, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Royal M', b.id, 'Collares', 319.00, 259.00, 11, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Royal L', b.id, 'Collares', 319.00, 259.00, 3, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Partner Pata de Perro', b.id, 'Correas', 199.00, 139.00, 18, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Partner Binomio', b.id, 'Correas', 199.00, 139.00, 8, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Correa Alerta Simbolo', b.id, 'Correas', 199.00, 129.00, 50, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Correa Binomio', b.id, 'Correas', 220.00, 149.00, 53, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Correa Pata de Perro', b.id, 'Correas', 220.00, 149.00, 66, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Bolsita', b.id, 'Accesorios', 80.00, 40.00, 116, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Porta Alerta', b.id, 'Accesorios', 169.00, 99.00, 71, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera Basica', b.id, 'Accesorios', 140.00, 140.00, 10, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera XS', b.id, 'Accesorios', 309.00, 189.00, 57, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera S', b.id, 'Accesorios', 309.00, 189.00, 64, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera M', b.id, 'Accesorios', 309.00, 189.00, 36, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera L', b.id, 'Accesorios', 319.00, 189.00, 7, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Basico', b.id, 'Accesorios', 140.00, 140.00, 0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Mochila S', b.id, 'Mochilas', 259.00, 175.00, 20, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Mochila M', b.id, 'Mochilas', 259.00, 175.00, 31, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Mochila L', b.id, 'Mochilas', 259.00, 175.00, 9, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Caminandog 3 MTS', b.id, 'Correas', 288.00, 288.00, 0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Porta Tag', b.id, 'Accesorios', 169.00, 99.00, 16, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Michito XS', b.id, 'Collares', 100.00, 79.00, 12, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Michito S', b.id, 'Collares', 100.00, 79.00, 15, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';

DO $$
DECLARE
    active_count INT;
BEGIN
    SELECT COUNT(*) INTO active_count FROM products WHERE is_active = TRUE;
    IF active_count <> 48 THEN
        RAISE EXCEPTION 'Migration V20 aborted: expected 48 active products after import, found %.', active_count;
    END IF;
END $$;
