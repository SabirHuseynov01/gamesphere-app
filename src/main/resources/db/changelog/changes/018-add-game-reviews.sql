--liquibase formatted sql

--changeset gamesphere:018-add-game-reviews
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS game_id BIGINT;

UPDATE reviews r
SET game_id = p.game_id
FROM products p
WHERE r.product_id = p.id
  AND r.game_id IS NULL
  AND p.game_id IS NOT NULL;

ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_game
    FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_reviews_game_approved
    ON reviews (game_id, approved);

CREATE UNIQUE INDEX IF NOT EXISTS uk_reviews_game_user
    ON reviews (game_id, user_id)
    WHERE game_id IS NOT NULL;

--rollback DROP INDEX IF EXISTS uk_reviews_game_user;
--rollback DROP INDEX IF EXISTS idx_reviews_game_approved;
--rollback ALTER TABLE reviews DROP CONSTRAINT IF EXISTS fk_reviews_game;
--rollback ALTER TABLE reviews DROP COLUMN IF EXISTS game_id;
