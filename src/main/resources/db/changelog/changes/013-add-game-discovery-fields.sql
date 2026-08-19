--liquibase formatted sql

--changeset gamesphere:013-add-game-discovery-fields
ALTER TABLE games
    ADD COLUMN access_type VARCHAR(255) NOT NULL DEFAULT 'PAID';

CREATE TABLE game_genres (
                             game_id BIGINT NOT NULL,
                             genre VARCHAR(255) NOT NULL,
                             CONSTRAINT pk_game_genres PRIMARY KEY (game_id, genre),
                             CONSTRAINT fk_game_genres_game FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE
);

CREATE TABLE game_supported_platforms (
                                          game_id BIGINT NOT NULL,
                                          platform VARCHAR(255) NOT NULL,
                                          CONSTRAINT pk_game_supported_platforms PRIMARY KEY (game_id, platform),
                                          CONSTRAINT fk_game_supported_platforms_game FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE
);

CREATE INDEX idx_game_genres_genre ON game_genres (genre);
CREATE INDEX idx_game_supported_platforms_platform ON game_supported_platforms (platform);

--rollback DROP INDEX IF EXISTS idx_game_supported_platforms_platform;
--rollback DROP INDEX IF EXISTS idx_game_genres_genre;
--rollback DROP TABLE IF EXISTS game_supported_platforms;
--rollback DROP TABLE IF EXISTS game_genres;
--rollback ALTER TABLE games DROP COLUMN IF EXISTS access_type;