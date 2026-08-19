--liquibase formatted sql

--changeset gamesphere:012-change-review-rating-to-decimal
ALTER TABLE reviews DROP CONSTRAINT ck_reviews_rating_range;
ALTER TABLE reviews
    ALTER COLUMN rating TYPE DOUBLE PRECISION
        USING rating::DOUBLE PRECISION;
ALTER TABLE reviews
    ADD CONSTRAINT ck_reviews_rating_range
        CHECK (
            rating BETWEEN 1.0 AND 5.0
                AND rating * 2 = FLOOR(rating * 2)
            );

--rollback ALTER TABLE reviews DROP CONSTRAINT ck_reviews_rating_range;
--rollback ALTER TABLE reviews ALTER COLUMN rating TYPE INTEGER USING rating::INTEGER;
--rollback ALTER TABLE reviews ADD CONSTRAINT ck_reviews_rating_range CHECK (rating BETWEEN 1 AND 5);