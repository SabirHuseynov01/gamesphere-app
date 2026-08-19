--liquibase formatted sql

--changeset gamesphere:006-seed-system-roles
INSERT INTO roles (created_at, updated_at, is_deleted, name)
VALUES
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 'ROLE_USER'),
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 'ROLE_SELLER'),
    (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, 'ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

--rollback DELETE FROM roles WHERE name IN ('ROLE_USER', 'ROLE_SELLER', 'ROLE_ADMIN');