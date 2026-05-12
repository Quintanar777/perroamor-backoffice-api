-- Limpia los datos de prueba del catálogo respetando el orden de FKs.
-- sales → CASCADE → sale_items (que referencia products/variants)
-- combos → CASCADE → combo_items (que referencia products/variants)

DELETE FROM sales;
DELETE FROM combos;
DELETE FROM product_variants;
DELETE FROM products;
DELETE FROM brands;

ALTER SEQUENCE brands_id_seq          RESTART WITH 1;
ALTER SEQUENCE products_id_seq        RESTART WITH 1;
ALTER SEQUENCE product_variants_id_seq RESTART WITH 1;
ALTER SEQUENCE combos_id_seq          RESTART WITH 1;
ALTER SEQUENCE sales_id_seq           RESTART WITH 1;
ALTER SEQUENCE sale_items_id_seq      RESTART WITH 1;
ALTER SEQUENCE combo_items_id_seq     RESTART WITH 1;