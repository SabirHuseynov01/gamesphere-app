--liquibase formatted sql

--changeset gamesphere:019-add-product-discount-window
ALTER TABLE products ADD COLUMN IF NOT EXISTS discount_start TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE products ADD COLUMN IF NOT EXISTS discount_end TIMESTAMP WITHOUT TIME ZONE;

--rollback ALTER TABLE products DROP COLUMN IF EXISTS discount_end;
--rollback ALTER TABLE products DROP COLUMN IF EXISTS discount_start;
