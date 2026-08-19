--liquibase formatted sql

--changeset gamesphere:005-add-query-indexes

CREATE INDEX idx_games_title_lower ON games (LOWER(title));
CREATE INDEX idx_games_developer ON games (developer);
CREATE INDEX idx_games_publisher ON games (publisher);

CREATE INDEX idx_products_game ON products (game_id);
CREATE INDEX idx_products_seller ON products (seller_id);
CREATE INDEX idx_products_name_lower ON products (LOWER(name));
CREATE INDEX idx_products_offer_filter ON products (game_id, product_status, is_active, platform, product_type);
CREATE INDEX idx_products_price ON products (price);
CREATE INDEX idx_products_last_checked ON products (last_checked_at);

CREATE INDEX idx_cart_items_cart ON cart_items (cart_id);
CREATE INDEX idx_wishlist_items_wishlist ON wishlist_items (wishlist_id);
CREATE INDEX idx_orders_user_created ON orders (user_id, created_at DESC);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_order_items_order ON order_items (order_id);
CREATE INDEX idx_payments_user ON payments (user_id);

CREATE INDEX idx_gifts_recipient_status ON gifts (LOWER(recipient_email), status);
CREATE INDEX idx_gifts_sender_created ON gifts (sender_id, created_at DESC);
CREATE INDEX idx_entitlements_user_status ON user_entitlements (user_id, status, granted_at DESC);

CREATE INDEX idx_notifications_user_status ON notifications (user_id, status, created_at DESC);
CREATE INDEX idx_reviews_product_approved ON reviews (product_id, approved);
CREATE INDEX idx_tournaments_status_start ON tournaments (status, start_date);
CREATE INDEX idx_tournament_participants_tournament ON tournament_participants (tournament_id);

--rollback DROP INDEX IF EXISTS idx_tournament_participants_tournament;
--rollback DROP INDEX IF EXISTS idx_tournaments_status_start;
--rollback DROP INDEX IF EXISTS idx_reviews_product_approved;
--rollback DROP INDEX IF EXISTS idx_notifications_user_status;
--rollback DROP INDEX IF EXISTS idx_entitlements_user_status;
--rollback DROP INDEX IF EXISTS idx_gifts_sender_created;
--rollback DROP INDEX IF EXISTS idx_gifts_recipient_status;
--rollback DROP INDEX IF EXISTS idx_payments_user;
--rollback DROP INDEX IF EXISTS idx_order_items_order;
--rollback DROP INDEX IF EXISTS idx_orders_status;
--rollback DROP INDEX IF EXISTS idx_orders_user_created;
--rollback DROP INDEX IF EXISTS idx_wishlist_items_wishlist;
--rollback DROP INDEX IF EXISTS idx_cart_items_cart;
--rollback DROP INDEX IF EXISTS idx_products_last_checked;
--rollback DROP INDEX IF EXISTS idx_products_price;
--rollback DROP INDEX IF EXISTS idx_products_offer_filter;
--rollback DROP INDEX IF EXISTS idx_products_name_lower;
--rollback DROP INDEX IF EXISTS idx_products_seller;
--rollback DROP INDEX IF EXISTS idx_products_game;
--rollback DROP INDEX IF EXISTS idx_games_publisher;
--rollback DROP INDEX IF EXISTS idx_games_developer;
--rollback DROP INDEX IF EXISTS idx_games_title_lower;

--changeset gamesphere:006-seed-system-roles.sql
INSERT INTO roles (created_at, updated_at, is_deleted, name)
VALUES
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 'ROLE_USER'),
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 'ROLE_SELLER'),
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 'ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

--rollback DELETE FROM roles WHERE name IN ('ROLE_USER', 'ROLE_SELLER', 'ROLE_ADMIN');