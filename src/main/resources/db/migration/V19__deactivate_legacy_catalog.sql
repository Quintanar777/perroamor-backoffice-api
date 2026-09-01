-- Retiro del catálogo legado: se respalda el estado activo actual y luego se
-- desactiva (isActive=false) todo producto, variante y combo existente.
-- No se elimina ninguna fila: sales/combos históricos dependen de FKs RESTRICT.

CREATE TABLE catalog_deactivation_backup (
    id          BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(20) NOT NULL,
    entity_id   BIGINT      NOT NULL,
    CONSTRAINT chk_backup_entity_type CHECK (entity_type IN ('PRODUCT', 'VARIANT', 'COMBO')),
    CONSTRAINT uq_backup_entity UNIQUE (entity_type, entity_id)
);

INSERT INTO catalog_deactivation_backup (entity_type, entity_id)
SELECT 'PRODUCT', id FROM products WHERE is_active = TRUE;

INSERT INTO catalog_deactivation_backup (entity_type, entity_id)
SELECT 'VARIANT', id FROM product_variants WHERE is_active = TRUE;

INSERT INTO catalog_deactivation_backup (entity_type, entity_id)
SELECT 'COMBO', id FROM combos WHERE is_active = TRUE;

UPDATE products         SET is_active = FALSE WHERE is_active = TRUE;
UPDATE product_variants SET is_active = FALSE WHERE is_active = TRUE;
UPDATE combos            SET is_active = FALSE WHERE is_active = TRUE;
