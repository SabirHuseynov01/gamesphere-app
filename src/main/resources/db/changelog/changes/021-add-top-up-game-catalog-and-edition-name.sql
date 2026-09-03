--liquibase formatted sql

--changeset gamesphere:021-add-top-up-game-catalog-and-edition-name
ALTER TABLE games
    ADD COLUMN IF NOT EXISTS catalog_type VARCHAR(32);

UPDATE games
SET catalog_type = 'GAME'
WHERE catalog_type IS NULL;

ALTER TABLE games
    ALTER COLUMN catalog_type SET DEFAULT 'GAME',
ALTER COLUMN catalog_type SET NOT NULL;

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS edition_name VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_games_catalog_type
    ON games (catalog_type, is_deleted);

--rollback DROP INDEX IF EXISTS idx_games_catalog_type;
--rollback ALTER TABLE products DROP COLUMN IF EXISTS edition_name;
--rollback ALTER TABLE games DROP COLUMN IF EXISTS catalog_type;