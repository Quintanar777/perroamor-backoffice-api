ALTER TABLE brands
    ADD COLUMN base_color VARCHAR(7);

ALTER TABLE brands
    ADD CONSTRAINT chk_brands_base_color_hex
        CHECK (base_color IS NULL OR base_color ~ '^#[0-9A-Fa-f]{6}$');
