--liquibase formatted sql

--changeset gamesphere:007-add-top-up-player-account-id
ALTER TABLE cart_items
    ADD COLUMN player_account_id VARCHAR(255);

ALTER TABLE order_items
    ADD COLUMN player_account_id VARCHAR(255);

CREATE INDEX idx_order_items_player_account_id
    ON order_items (player_account_id)
    WHERE player_account_id IS NOT NULL;

--rollback DROP INDEX IF EXISTS idx_order_items_player_account_id;
--rollback ALTER TABLE order_items DROP COLUMN IF EXISTS player_account_id;
--rollback ALTER TABLE cart_items DROP COLUMN IF EXISTS player_account_id;