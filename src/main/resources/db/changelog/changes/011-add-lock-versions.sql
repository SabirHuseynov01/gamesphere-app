--liquibase formatted sql

--changeset gamesphere:011-add-lock-versions
ALTER TABLE users ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE products ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

--rollback ALTER TABLE orders DROP COLUMN IF EXISTS version;
--rollback ALTER TABLE products DROP COLUMN IF EXISTS version;
--rollback ALTER TABLE users DROP COLUMN IF EXISTS version;