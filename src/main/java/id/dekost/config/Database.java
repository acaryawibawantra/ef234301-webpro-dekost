package id.dekost.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class Database {

    private static volatile HikariDataSource dataSource;

    private Database() {
    }

    private static HikariDataSource dataSource() {
        HikariDataSource local = dataSource;
        if (local == null) {
            synchronized (Database.class) {
                local = dataSource;
                if (local == null) {
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl(requiredEnv("DB_URL"));
                    config.setUsername(requiredEnv("DB_USER"));
                    config.setPassword(requiredEnv("DB_PASSWORD"));
                    config.setDriverClassName("org.postgresql.Driver");
                    config.setMaximumPoolSize(10);
                    config.setMinimumIdle(2);
                    config.setConnectionTimeout(10_000);
                    config.setPoolName("dekost-pool");
                    local = new HikariDataSource(config);
                    dataSource = local;
                }
            }
        }
        return local;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource().getConnection();
    }

    public static boolean ping() {
        try (Connection connection = getConnection();
             var statement = connection.prepareStatement("SELECT 1");
             var result = statement.executeQuery()) {
            return result.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private static String requiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable " + key + " must be set.");
        }
        return value;
    }
}
