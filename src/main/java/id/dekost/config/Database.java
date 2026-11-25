package id.dekost.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.net.URI;
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
                    config.setJdbcUrl(normalizeJdbcUrl(env("DB_URL", DEFAULT_URL)));
                    config.setUsername(env("DB_USER", DEFAULT_USER));
                    config.setPassword(env("DB_PASSWORD", DEFAULT_PASSWORD));
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

    static String normalizeJdbcUrl(String value) {
        if (value.startsWith("jdbc:postgresql://")) {
            return value;
        }

        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            if (!"postgres".equalsIgnoreCase(scheme)
                    && !"postgresql".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException();
            }

            String host = uri.getHost();
            String path = uri.getRawPath();
            if (host == null || path == null || path.length() < 2) {
                throw new IllegalArgumentException();
            }
            if (host.indexOf(':') >= 0 && !host.startsWith("[")) {
                host = "[" + host + "]";
            }

            int port = uri.getPort() < 0 ? 5432 : uri.getPort();
            String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
            return "jdbc:postgresql://" + host + ":" + port + path + query;
        } catch (IllegalArgumentException ignored) {
            throw new IllegalStateException(
                    "DB_URL must be a PostgreSQL JDBC URL or PostgreSQL connection URL.");
        }
    }
}
