package com.example.gamesphere.integration;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LiquibaseChangelogTest {

    private static final String CHANGELOG = "db/changelog/db.changelog-master.yaml";

    @Test
    void masterChangelogLoadsAllChangesets() throws Exception {
        try (ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor()) {
            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance()
                    .getParser(CHANGELOG, resourceAccessor)
                    .parse(CHANGELOG, new ChangeLogParameters(), resourceAccessor);

            assertThat(changeLog.getChangeSets())
                    .extracting(changeSet -> changeSet.getId())
                    .containsExactly(
                            "001-create-core-schema",
                            "002-create-catalog-schema",
                            "003-create-commerce-schema",
                            "004-create-community-schema",
                            "005-add-query-indexes",
                            "006-seed-system-roles.sql",
                            "006-seed-system-roles",
                            "007-add-top-up-player-account-id",
                            "008-create-top-up-fulfillments",
                            "009-add-refresh-tokens",
                            "010-add-digital-delivery",
                            "011-add-lock-versions",
                            "012-change-review-rating-to-decimal",
                            "013-add-game-discovery-fields",
                            "014-add-catalog-section",
                            "015-add-product-edition-name",
                            "016-prevent-duplicate-edition-offers",
                            "017-restore-non-stock-managed-offers",
                            "018-add-game-reviews",
                            "019-add-product-discount-window");
        }
    }
}
