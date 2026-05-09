-- Consolida product.stock como contador maestro para productos con variantes.
-- Antes de esta migración, product.stock era 0 para productos con hasVariants=true
-- (el stock real vivía en cada variant.stock). A partir de aquí, product.stock es el
-- contador maestro y se sincroniza con sum(variants.stock) en este punto inicial.
--
-- En adelante (post-migración):
--   - Vender SIN variantId → descuenta solo product.stock.
--   - Vender CON variantId → descuenta variant.stock Y product.stock (atómico, en SaleService).
--   - Cancelar es simétrico (restituye al/los lugar(es) correspondiente(s)).
--
-- Productos sin variantes no se afectan (sigue siendo el mismo product.stock que ya tenían).

UPDATE products
SET stock = COALESCE(
    (SELECT SUM(v.stock)
     FROM product_variants v
     WHERE v.product_id = products.id AND v.is_active = true),
    products.stock
)
WHERE has_variants = true;
