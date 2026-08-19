--liquibase formatted sql

--changeset gamesphere:015-add-product-edition-name
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS edition_name VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_products_game_edition
    ON products (game_id, edition_name);

--rollback DROP INDEX IF EXISTS idx_products_game_edition;
--rollback ALTER TABLE products DROP COLUMN IF EXISTS edition_name;
