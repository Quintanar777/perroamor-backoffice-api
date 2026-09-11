-- Da de alta los descuentos (feature que reemplaza a "Combos", ver V17) a partir
-- de repositories/specs/inventory_2_0/combos_v2.png: kit Todo Mio, kit Quiero
-- Todo, Combo Caminata, Combo Paseo, Collar+Correa Mini Cake y Collar+Correa
-- Fancy. No migra los combos.png anteriores (ver spec.md "Migración de los kits
-- actuales") ni las filas de combos_v2.png con PRECIO ≠ PRECIO EXPO (esa
-- columna no existe en combos_v2.png, ya no aplica).
--
-- Cada producto se referencia por name (+ size cuando aplica) contra la tabla
-- products ya cargada por V20, nunca por id fijo.
--
-- kit Todo Mio y kit Quiero Todo aparecen dos veces en la imagen con el mismo
-- precio total pero receta distinta (Collar Vida Mia/Correa Pata de Perro vs.
-- Collar Milagro/Correa Binomio) -- se dan de alta como dos discounts
-- independientes con el mismo nombre, no como un solo discount con los cuatro
-- productos agrupados: agruparlos permitiría combinaciones no listadas en la
-- imagen (ej. Vida Mia + Binomio), y el matcher no tiene forma de restringir
-- eso entre slots.
--
-- "Pechera" en la imagen no trae talla, pero el catálogo tiene "Pechera"
-- (XS/S/M/L, $309-$319) y "Pechera Basica" ($140, sin talla) por separado. Se
-- asume que se refiere a "Pechera" con talla: con "Pechera Basica" el kit
-- costaría más que comprar todo suelto (no sería un descuento); con "Pechera"
-- con talla sí hay ahorro real frente a la suma de precios de lista.
--
-- Reparto de precio por slot: un slot GROUP aplica el mismo final_unit_price a
-- todas sus opciones (collar/pechera/mochila de cualquier talla), porque el
-- matcher no tiene forma de ajustar el precio de un slot según lo elegido en
-- otro slot -- es la única forma de que la suma dé el total configurado sin
-- importar qué talla entra al carrito. El precio final por slot se calculó
-- proporcional al precio de lista de cada producto (talla más barata del grupo
-- como referencia), redondeado a pesos enteros con el remanente repartido por
-- mayor parte fraccionaria, para que cada combinación válida sume exacto el
-- total configurado.

DO $$
DECLARE
    v_discount_id BIGINT;
    v_slot_id BIGINT;
BEGIN
    -- Kit Todo Mio (A): Collar Vida Mia / Correa Pata de Perro / Pechera / Bolsita = $699
    INSERT INTO discounts (name, description, total_price, is_active)
    VALUES ('Kit Todo Mio', 'Collar Vida Mia (cualquier talla) + Correa Pata de Perro + Pechera (cualquier talla) + Bolsita', 699.00, TRUE)
    RETURNING id INTO v_discount_id;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 1, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 159.00 FROM products p WHERE p.name = 'Collar Vida Mia' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 2, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 195.00 FROM products p WHERE p.name = 'Correa Pata de Perro' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 3, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 274.00 FROM products p WHERE p.name = 'Pechera' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 4, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 71.00 FROM products p WHERE p.name = 'Bolsita' AND p.is_active = TRUE;
END $$;

DO $$
DECLARE
    v_discount_id BIGINT;
    v_slot_id BIGINT;
BEGIN
    -- Kit Todo Mio (B): Collar Milagro / Correa Binomio / Pechera / Bolsita = $699
    INSERT INTO discounts (name, description, total_price, is_active)
    VALUES ('Kit Todo Mio', 'Collar Milagro (cualquier talla) + Correa Binomio + Pechera (cualquier talla) + Bolsita', 699.00, TRUE)
    RETURNING id INTO v_discount_id;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 1, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 159.00 FROM products p WHERE p.name = 'Collar Milagro' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 2, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 195.00 FROM products p WHERE p.name = 'Correa Binomio' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 3, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 274.00 FROM products p WHERE p.name = 'Pechera' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 4, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 71.00 FROM products p WHERE p.name = 'Bolsita' AND p.is_active = TRUE;
END $$;

DO $$
DECLARE
    v_discount_id BIGINT;
    v_slot_id BIGINT;
BEGIN
    -- Kit Quiero Todo (A): Collar Vida Mia / Correa Pata de Perro / Pechera / Bolsita / Mochila = $989
    INSERT INTO discounts (name, description, total_price, is_active)
    VALUES ('Kit Quiero Todo', 'Collar Vida Mia (cualquier talla) + Correa Pata de Perro + Pechera (cualquier talla) + Bolsita + Mochila (cualquier talla)', 989.00, TRUE)
    RETURNING id INTO v_discount_id;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 1, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 169.00 FROM products p WHERE p.name = 'Collar Vida Mia' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 2, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 208.00 FROM products p WHERE p.name = 'Correa Pata de Perro' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 3, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 292.00 FROM products p WHERE p.name = 'Pechera' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 4, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 75.00 FROM products p WHERE p.name = 'Bolsita' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 5, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 245.00 FROM products p WHERE p.name = 'Mochila' AND p.is_active = TRUE;
END $$;

DO $$
DECLARE
    v_discount_id BIGINT;
    v_slot_id BIGINT;
BEGIN
    -- Kit Quiero Todo (B): Collar Milagro / Correa Binomio / Pechera / Bolsita / Mochila = $989
    INSERT INTO discounts (name, description, total_price, is_active)
    VALUES ('Kit Quiero Todo', 'Collar Milagro (cualquier talla) + Correa Binomio + Pechera (cualquier talla) + Bolsita + Mochila (cualquier talla)', 989.00, TRUE)
    RETURNING id INTO v_discount_id;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 1, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 169.00 FROM products p WHERE p.name = 'Collar Milagro' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 2, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 208.00 FROM products p WHERE p.name = 'Correa Binomio' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 3, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 292.00 FROM products p WHERE p.name = 'Pechera' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 4, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 75.00 FROM products p WHERE p.name = 'Bolsita' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 5, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 245.00 FROM products p WHERE p.name = 'Mochila' AND p.is_active = TRUE;
END $$;

DO $$
DECLARE
    v_discount_id BIGINT;
    v_slot_id BIGINT;
BEGIN
    -- Combo Caminata (A): Pechera / Correa Pata de Perro / Bolsita = $579
    INSERT INTO discounts (name, description, total_price, is_active)
    VALUES ('Combo Caminata', 'Pechera (cualquier talla) + Correa Pata de Perro + Bolsita', 579.00, TRUE)
    RETURNING id INTO v_discount_id;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 1, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 294.00 FROM products p WHERE p.name = 'Pechera' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 2, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 209.00 FROM products p WHERE p.name = 'Correa Pata de Perro' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 3, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 76.00 FROM products p WHERE p.name = 'Bolsita' AND p.is_active = TRUE;
END $$;

DO $$
DECLARE
    v_discount_id BIGINT;
    v_slot_id BIGINT;
BEGIN
    -- Combo Caminata (B): Pechera / Correa Binomio / Bolsita = $579
    INSERT INTO discounts (name, description, total_price, is_active)
    VALUES ('Combo Caminata', 'Pechera (cualquier talla) + Correa Binomio + Bolsita', 579.00, TRUE)
    RETURNING id INTO v_discount_id;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 1, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 294.00 FROM products p WHERE p.name = 'Pechera' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 2, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 209.00 FROM products p WHERE p.name = 'Correa Binomio' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 3, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 76.00 FROM products p WHERE p.name = 'Bolsita' AND p.is_active = TRUE;
END $$;

DO $$
DECLARE
    v_discount_id BIGINT;
    v_slot_id BIGINT;
BEGIN
    -- Combo Paseo (A): Collar Vida Mia / Correa Pata de Perro / Bolsita = $449
    INSERT INTO discounts (name, description, total_price, is_active)
    VALUES ('Combo Paseo', 'Collar Vida Mia (cualquier talla) + Correa Pata de Perro + Bolsita', 449.00, TRUE)
    RETURNING id INTO v_discount_id;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 1, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 168.00 FROM products p WHERE p.name = 'Collar Vida Mia' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 2, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 206.00 FROM products p WHERE p.name = 'Correa Pata de Perro' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 3, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 75.00 FROM products p WHERE p.name = 'Bolsita' AND p.is_active = TRUE;
END $$;

DO $$
DECLARE
    v_discount_id BIGINT;
    v_slot_id BIGINT;
BEGIN
    -- Combo Paseo (B): Collar Milagro / Correa Binomio / Bolsita = $449
    INSERT INTO discounts (name, description, total_price, is_active)
    VALUES ('Combo Paseo', 'Collar Milagro (cualquier talla) + Correa Binomio + Bolsita', 449.00, TRUE)
    RETURNING id INTO v_discount_id;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 1, 'GROUP', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 168.00 FROM products p WHERE p.name = 'Collar Milagro' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 2, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 206.00 FROM products p WHERE p.name = 'Correa Binomio' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 3, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 75.00 FROM products p WHERE p.name = 'Bolsita' AND p.is_active = TRUE;
END $$;

DO $$
DECLARE
    v_discount_id BIGINT;
    v_slot_id BIGINT;
BEGIN
    -- Collar + Correa Mini Cake = $149
    INSERT INTO discounts (name, description, total_price, is_active)
    VALUES ('Collar + Correa Mini Cake', 'Collar Mini Cake + Correa Mini Cake', 149.00, TRUE)
    RETURNING id INTO v_discount_id;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 1, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 61.00 FROM products p WHERE p.name = 'Collar Mini Cake' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 2, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 88.00 FROM products p WHERE p.name = 'Correa Mini Cake' AND p.is_active = TRUE;
END $$;

DO $$
DECLARE
    v_discount_id BIGINT;
    v_slot_id BIGINT;
BEGIN
    -- Collar + Correa Fancy = $299
    INSERT INTO discounts (name, description, total_price, is_active)
    VALUES ('Collar + Correa Fancy', 'Collar Fancy + Correa Fancy', 299.00, TRUE)
    RETURNING id INTO v_discount_id;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 1, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 123.00 FROM products p WHERE p.name = 'Collar Fancy' AND p.is_active = TRUE;

    INSERT INTO discount_slots (discount_id, position, slot_type, quantity)
    VALUES (v_discount_id, 2, 'FIXED', 1) RETURNING id INTO v_slot_id;
    INSERT INTO discount_slot_options (slot_id, product_id, final_unit_price)
    SELECT v_slot_id, p.id, 176.00 FROM products p WHERE p.name = 'Correa Fancy' AND p.is_active = TRUE;
END $$;

DO $$
DECLARE
    discount_count INT;
    slot_count INT;
    option_count INT;
    bad_group_count INT;
    bad_fixed_count INT;
BEGIN
    SELECT COUNT(*) INTO discount_count FROM discounts;
    IF discount_count <> 10 THEN
        RAISE EXCEPTION 'Migration V21 aborted: expected 10 discounts after import, found %.', discount_count;
    END IF;

    SELECT COUNT(*) INTO slot_count FROM discount_slots ds JOIN discounts d ON d.id = ds.discount_id;
    IF slot_count <> 34 THEN
        RAISE EXCEPTION 'Migration V21 aborted: expected 34 discount_slots after import, found %.', slot_count;
    END IF;

    SELECT COUNT(*) INTO option_count FROM discount_slot_options dso JOIN discount_slots ds ON ds.id = dso.slot_id;
    IF option_count <> 68 THEN
        RAISE EXCEPTION 'Migration V21 aborted: expected 68 discount_slot_options after import, found %.', option_count;
    END IF;

    -- Cada slot GROUP debe tener sus opciones al mismo final_unit_price (ver nota arriba).
    SELECT COUNT(*) INTO bad_group_count
    FROM discount_slots ds
    WHERE ds.slot_type = 'GROUP'
      AND (SELECT COUNT(DISTINCT dso.final_unit_price) FROM discount_slot_options dso WHERE dso.slot_id = ds.id) <> 1;
    IF bad_group_count <> 0 THEN
        RAISE EXCEPTION 'Migration V21 aborted: % GROUP slot(s) with inconsistent final_unit_price across options.', bad_group_count;
    END IF;

    -- Cada slot FIXED debe tener exactamente una opción.
    SELECT COUNT(*) INTO bad_fixed_count
    FROM discount_slots ds
    WHERE ds.slot_type = 'FIXED'
      AND (SELECT COUNT(*) FROM discount_slot_options dso WHERE dso.slot_id = ds.id) <> 1;
    IF bad_fixed_count <> 0 THEN
        RAISE EXCEPTION 'Migration V21 aborted: % FIXED slot(s) without exactly one option.', bad_fixed_count;
    END IF;

    -- Cada discount debe sumar exacto su total_price para al menos una combinación (mínimo por slot).
    IF EXISTS (
        SELECT d.id
        FROM discounts d
        WHERE d.total_price <> (
            SELECT SUM(min_price)
            FROM (
                SELECT ds.id, MIN(dso.final_unit_price) AS min_price
                FROM discount_slots ds
                JOIN discount_slot_options dso ON dso.slot_id = ds.id
                WHERE ds.discount_id = d.id
                GROUP BY ds.id
            ) per_slot
        )
    ) THEN
        RAISE EXCEPTION 'Migration V21 aborted: some discount total_price does not match the sum of its slot prices.';
    END IF;
END $$;
