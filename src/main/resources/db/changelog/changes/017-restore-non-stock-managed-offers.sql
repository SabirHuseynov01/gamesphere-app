--liquibase formatted sql

--changeset gamesphere:017-restore-non-stock-managed-offers
-- Redirect and external-market offers do not consume local inventory.
UPDATE products
SET product_status = 'ACTIVE',
    is_active = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE is_deleted = FALSE
  AND product_status = 'OUT_OF_STOCK'
  AND delivery_type IN ('STORE_REDIRECT', 'EXTERNAL_MARKET', 'PLAYER_ID_TOP_UP');

--rollback UPDATE products
--rollback SET product_status = 'OUT_OF_STOCK'
--rollback WHERE delivery_type IN ('STORE_REDIRECT', 'EXTERNAL_MARKET', 'PLAYER_ID_TOP_UP')
--rollback   AND is_deleted = FALSE;
