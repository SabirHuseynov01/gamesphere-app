--liquibase formatted sql

--changeset gamesphere:016-prevent-duplicate-edition-offers
CREATE UNIQUE INDEX IF NOT EXISTS uk_products_game_platform_store_edition
    ON products (
        game_id,
        platform,
        LOWER(store_name),
        LOWER(edition_name)
    )
    WHERE game_id IS NOT NULL
      AND edition_name IS NOT NULL
      AND is_deleted = FALSE;

--rollback DROP INDEX IF EXISTS uk_products_game_platform_store_edition;
