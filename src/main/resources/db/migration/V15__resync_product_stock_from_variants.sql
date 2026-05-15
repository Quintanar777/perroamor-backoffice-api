-- Re-sincroniza product.stock con sum(active_variant.stock) para productos con variantes.
-- V10 lo hizo una sola vez al migrar; esta migración corrige cualquier drift posterior
-- causado por creación/actualización de variantes que no actualizaba el contador maestro.
-- Para productos sin variantes activas, el stock queda en 0 (no en NULL).

UPDATE products
SET stock = COALESCE(
    (SELECT SUM(v.stock)
     FROM product_variants v
     WHERE v.product_id = products.id AND v.is_active = true),
    0
)
WHERE has_variants = true;
