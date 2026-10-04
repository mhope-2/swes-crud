package com.michaelhope.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class FlywayMigrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Test
    void migratesLegacyTechnologyValuesAndLeavesValidatedCurrentSchema() throws Exception {
        awaitDatabase();
        flyway(MigrationVersion.fromVersion("1")).migrate();

        int engineerId;
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(
                 "insert into software_engineer (name, tech_stack, aggregate_version) "
                     + "values (?, ?, ?) returning id")) {
            statement.setString(1, "Legacy Engineer");
            statement.setString(2, " Java, JAVA, Spring  Boot ");
            statement.setInt(3, 1);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                engineerId = resultSet.getInt(1);
            }
        }

        flyway(null).migrate();

        try (Connection connection = connection()) {
            assertThat(scalarInt(connection,
                "select count(*) from technology where normalized_name in ('java', 'spring boot')"))
                .isEqualTo(2);
            assertThat(scalarInt(connection,
                "select count(*) from software_engineer_technology where software_engineer_id = " + engineerId))
                .isEqualTo(2);
            assertThat(scalarInt(connection,
                "select count(*) from information_schema.columns "
                    + "where table_schema = current_schema() "
                    + "and table_name = 'software_engineer' "
                    + "and column_name = 'tech_stack'"))
                .isZero();
            assertThat(scalarInt(connection,
                "select count(*) from flyway_schema_history where success"))
                .isEqualTo(3);
            assertThat(tableNames(connection))
                .contains("software_engineer", "technology", "software_engineer_technology", "engineer_audit");
        }
    }

    private Flyway flyway(MigrationVersion target) {
        var configuration = Flyway.configure()
            .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
            .locations("classpath:db/migration");
        if (target != null) {
            configuration.target(target);
        }
        return configuration.load();
    }

    private Connection connection() throws Exception {
        return DriverManager.getConnection(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    private void awaitDatabase() throws Exception {
        long deadline = System.nanoTime() + 30_000_000_000L;
        SQLException lastFailure = null;
        while (System.nanoTime() < deadline) {
            try (Connection ignored = connection()) {
                return;
            } catch (SQLException exception) {
                lastFailure = exception;
                Thread.sleep(200);
            }
        }
        throw lastFailure;
    }

    private int scalarInt(Connection connection, String sql) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private List<String> tableNames(Connection connection) throws Exception {
        List<String> names = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
            "select table_name from information_schema.tables "
                + "where table_schema = current_schema() and table_type = 'BASE TABLE'")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    names.add(resultSet.getString(1));
                }
            }
        }
        return names;
    }
}
