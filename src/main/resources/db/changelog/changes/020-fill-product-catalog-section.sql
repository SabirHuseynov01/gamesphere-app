--liquibase formatted sql

--changeset gamesphere:020-fill-product-catalog-section

UPDATE products
SET catalog_section = CASE
                          WHEN product_type IN (
                                                'IN_GAME_CURRENCY',
                                                'IN_GAME_ITEM',
                                                'CURRENCY',
                                                'ITEM',
                                                'BATTLE_PASS',
                                                'GIFT_CARD',
                                                'SUBSCRIPTION'
                              )
                              THEN 'TOP_UPS'
                          ELSE 'MARKETPLACE'
    END
WHERE catalog_section IS NULL;

ALTER TABLE products
    ALTER COLUMN catalog_section SET DEFAULT 'MARKETPLACE',
ALTER COLUMN catalog_section SET NOT NULL;