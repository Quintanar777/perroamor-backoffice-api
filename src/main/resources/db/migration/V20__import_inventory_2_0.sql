-- Agrega la columna size (talla estructurada, separada de name) a products, y
-- hace la importación literal de inventario 2.0. Transcripción directa de
-- repositories/specs/inventory_2_0/inventario_1_4.csv (fuente congelada), las
-- 61 filas de datos completas. Cada fila CSV se importa como un Product
-- independiente (nunca ProductVariant): name = PRODUCTO recortado (sin la
-- talla); size = TALLA recortada, o NULL cuando la fila no trae talla; price
-- = PRECIO EXPO; wholesale_price = PRECIO MAYOREO, o PRECIO EXPO si viene
-- vacío; stock = CANTIDAD o 0. category se asigna explícitamente por fila
-- según el vocabulario existente (Collares, Correas, Mochilas, Accesorios).
--
-- Reemplaza la versión anterior de este script (basada en inventario_1_3, sin
-- código de escaneo): el inventario cambió antes de llegar a producción, así
-- que se reescribe en el lugar en vez de encadenar un V22 -- no hay ambiente
-- compartido que ya la haya corrido.
--
-- Cambios de inventario_1_3 a inventario_1_4: la columna ABR del CSV se usó
-- solo para calcular el código de escaneo de cada fila aquí abajo -- no se
-- guarda como columna propia (no hace falta conservarla: si en el futuro
-- alguien necesita re-derivar o editar un código, lo hace directo en el campo
-- code del producto). code = ABR en mayúsculas sin acentos, más "-" + TALLA
-- cuando la fila trae talla (ej. "PTR PP" -> "PTR-PP", "3 MTS" -> "3-MTS").
-- Las 61 combinaciones ABR+TALLA de este CSV ya son únicas por construcción.
-- Para productos nuevos (fuera de esta migración), ProductService.
-- generateCode() no usa ABR (no se guarda): usa las primeras 5 letras del
-- nombre + talla, editable después desde el formulario de producto.
--
-- Además "Collar Martingale Basico" pasa a llamarse "Martingale Basico" (se
-- quitó "Collar" del nombre). Ningún precio, mayoreo ni cantidad cambió.
--
-- Vanny Perlas sigue sin traer PRECIO EXPO en esta versión del CSV. Se da de
-- alta con price=0.00 y wholesale_price=0.00 para tener el producto creado de
-- una vez y completar el precio manualmente después.
--
-- NOTA sobre categoría: Juguete, Sereno Moreno, Oferta Verde, Oferta Roja y
-- Oferta Brillo no encajan en Collares/Correas/Mochilas -- se asignan a
-- Accesorios (categoría catch-all ya usada para Bolsita, Porta Alerta, Pechera,
-- Basico, Porta Tag). Frazada también se asigna a Accesorios. Ajustar
-- manualmente si corresponde otra categoría.

ALTER TABLE products ADD COLUMN size VARCHAR(40);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM brands WHERE name = 'Perro Amor') THEN
        RAISE EXCEPTION 'Migration V20 aborted: brand "Perro Amor" not found.';
    END IF;
END $$;

INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Milagro', 'S', 'MIL-S', b.id, 'Collares', 179.00, 109.00, 89, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Milagro', 'M', 'MIL-M', b.id, 'Collares', 189.00, 119.00, 80, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Milagro', 'L', 'MIL-L', b.id, 'Collares', 189.00, 119.00, 41, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vida Mia', 'S', 'VM-S', b.id, 'Collares', 179.00, 109.00, 80, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vida Mia', 'M', 'VM-M', b.id, 'Collares', 189.00, 119.00, 75, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vida Mia', 'L', 'VM-L', b.id, 'Collares', 189.00, 119.00, 49, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Alebrije', 'XS', 'ALB-XS', b.id, 'Collares', 219.00, 149.00, 10, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Alebrije', 'S', 'ALB-S', b.id, 'Collares', 219.00, 149.00, 12, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Alebrije', 'M', 'ALB-M', b.id, 'Collares', 219.00, 149.00, 21, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Alebrije', 'L', 'ALB-L', b.id, 'Collares', 219.00, 149.00, 15, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vanny', 'XS', 'VAN-XS', b.id, 'Collares', 189.00, 119.00, 3, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vanny', 'S', 'VAN-S', b.id, 'Collares', 189.00, 119.00, 4, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vanny', 'M', 'VAN-M', b.id, 'Collares', 189.00, 119.00, 1, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Vanny', 'L', 'VAN-L', b.id, 'Collares', 189.00, 119.00, 1, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Deja Vu Negro', 'M', 'DVNEG-M', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Deja Vu Negro', 'L', 'DVNEG-L', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Deja Vu Rojo', 'M', 'DVROJ-M', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Deja Vu Rojo', 'L', 'DVROJ-L', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Deja Vu', 'M', 'DV-M', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Deja Vu', 'L', 'DV-L', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Abuelita', 'M', 'ABU-M', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Abuelita', 'L', 'ABU-L', b.id, 'Collares', 249.00, 189.00, 2, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Lacito', 'M', 'LAC-M', b.id, 'Collares', 189.00, 119.00, 4, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Lacito', 'L', 'LAC-L', b.id, 'Collares', 189.00, 119.00, 3, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Royal', 'XS', 'ROY-XS', b.id, 'Collares', 299.00, 259.00, 1, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Royal', 'S', 'ROY-S', b.id, 'Collares', 299.00, 259.00, 8, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Royal', 'M', 'ROY-M', b.id, 'Collares', 319.00, 259.00, 11, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Royal', 'L', 'ROY-L', b.id, 'Collares', 319.00, 259.00, 3, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Gatito', NULL, 'CGATI', b.id, 'Collares', 80.00, 60.00, 10, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Michito', 'XS', 'MICH-XS', b.id, 'Collares', 100.00, 79.00, 19, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Michito', 'S', 'MICH-S', b.id, 'Collares', 100.00, 79.00, 27, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Partner Pata de Perro', NULL, 'PTR-PP', b.id, 'Correas', 199.00, 139.00, 18, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Partner Binomio', NULL, 'PTR-BI', b.id, 'Correas', 199.00, 139.00, 8, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Correa Alerta Simbolo', NULL, 'CSIM', b.id, 'Correas', 199.00, 129.00, 50, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Correa Binomio', NULL, 'BIN', b.id, 'Correas', 220.00, 149.00, 53, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Correa Pata de Perro', NULL, 'PP', b.id, 'Correas', 220.00, 149.00, 66, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Caminandog', '3 MTS', 'CMDOG-3-MTS', b.id, 'Correas', 288.00, 219.00, 4, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Mochila', 'S', 'MOCH-S', b.id, 'Mochilas', 259.00, 175.00, 20, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Mochila', 'M', 'MOCH-M', b.id, 'Mochilas', 259.00, 175.00, 31, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Mochila', 'L', 'MOCH-L', b.id, 'Mochilas', 259.00, 175.00, 9, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Bolsita', NULL, 'BOL', b.id, 'Accesorios', 80.00, 40.00, 116, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Porta Alerta', NULL, 'PA', b.id, 'Accesorios', 169.00, 99.00, 71, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera Basica', NULL, 'PECHB', b.id, 'Accesorios', 140.00, 89.00, 10, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera', 'XS', 'PECH-XS', b.id, 'Accesorios', 309.00, 189.00, 57, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera', 'S', 'PECH-S', b.id, 'Accesorios', 309.00, 189.00, 64, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera', 'M', 'PECH-M', b.id, 'Accesorios', 309.00, 189.00, 36, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Pechera', 'L', 'PECH-L', b.id, 'Accesorios', 319.00, 189.00, 7, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Basico', NULL, 'BASICO', b.id, 'Accesorios', 140.00, 89.00, 20, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Porta Tag', NULL, 'PT', b.id, 'Accesorios', 169.00, 99.00, 16, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Juguete', NULL, 'JGT', b.id, 'Accesorios', 129.00, 129.00, 4, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Sereno Moreno', NULL, 'SM', b.id, 'Accesorios', 340.00, 239.00, 10, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Oferta Verde', NULL, 'OFV', b.id, 'Accesorios', 120.00, 120.00, 24, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Oferta Roja', NULL, 'OFR', b.id, 'Accesorios', 189.00, 189.00, 45, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Frazada', NULL, 'FRZ', b.id, 'Accesorios', 149.00, 149.00, 0, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Martingale Basico', NULL, 'MTL-B', b.id, 'Collares', 149.00, 149.00, 24, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Fancy', NULL, 'VMFAN', b.id, 'Collares', 139.00, 119.00, 27, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Correa Fancy', NULL, 'PPFAN', b.id, 'Correas', 199.00, 179.00, 18, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Collar Mini Cake', NULL, 'VANCAKE', b.id, 'Collares', 69.00, 59.00, 45, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Correa Mini Cake', NULL, 'PPCAKE', b.id, 'Correas', 99.00, 89.00, 29, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Vanny Perlas', 'M', 'VANPER-M', b.id, 'Collares', 0.00, 0.00, 4, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';
INSERT INTO products (name, size, code, brand_id, category, price, wholesale_price, stock, description, can_be_personalized, has_variants)
SELECT 'Oferta Brillo', NULL, 'OFBM', b.id, 'Accesorios', 150.00, 150.00, 4, NULL, FALSE, FALSE FROM brands b WHERE b.name = 'Perro Amor';

DO $$
DECLARE
    active_count INT;
    missing_code_count INT;
    duplicate_code_count INT;
BEGIN
    SELECT COUNT(*) INTO active_count FROM products WHERE is_active = TRUE;
    IF active_count <> 61 THEN
        RAISE EXCEPTION 'Migration V20 aborted: expected 61 active products after import, found %.', active_count;
    END IF;

    SELECT COUNT(*) INTO missing_code_count FROM products WHERE is_active = TRUE AND code IS NULL;
    IF missing_code_count <> 0 THEN
        RAISE EXCEPTION 'Migration V20 aborted: % active product(s) imported without a code.', missing_code_count;
    END IF;

    SELECT COUNT(*) INTO duplicate_code_count
    FROM (SELECT code FROM products WHERE is_active = TRUE GROUP BY code HAVING COUNT(*) > 1) d;
    IF duplicate_code_count <> 0 THEN
        RAISE EXCEPTION 'Migration V20 aborted: % duplicate code(s) among imported products.', duplicate_code_count;
    END IF;
END $$;
