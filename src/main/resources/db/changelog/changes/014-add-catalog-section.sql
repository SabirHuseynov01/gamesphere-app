--liquibase formatted sql

--changeset gamesphere:014-add-catalog-section
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS catalog_section VARCHAR(32);

UPDATE products
SET catalog_section = CASE
    WHEN product_type IN ('IN_GAME_CURRENCY', 'IN_GAME_ITEM', 'BATTLE_PASS', 'GIFT_CARD', 'SUBSCRIPTION')
        THEN 'TOP_UPS'
    ELSE 'MARKETPLACE'
END
WHERE catalog_section IS NULL;

ALTER TABLE products
    ALTER COLUMN catalog_section SET DEFAULT 'MARKETPLACE',
    ALTER COLUMN catalog_section SET NOT NULL;

CREATE INDEX idx_products_catalog_section
    ON products (catalog_section, product_status, is_active);
