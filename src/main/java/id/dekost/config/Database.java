package id.dekost.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Sumber koneksi database (HikariCP). Konfigurasi dibaca dari environment
 * variable dengan default untuk pengembangan lokal memakai docker compose.
 */
public final class Database {

    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/dekost";
    private static final String DEFAULT_USER = "dekost";
    private static final String DEFAULT_PASSWORD = "dekost";

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
                    config.setJdbcUrl(env("DB_URL", DEFAULT_URL));
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

    /**
     * Mengambil koneksi dari connection pool. Pemanggil wajib menutup koneksi
     * (try-with-resources) agar dikembalikan ke pool.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource().getConnection();
    }

    /**
     * Cek konektivitas database.
     *
     * @return true jika query {@code SELECT 1} berhasil.
     */
    public static boolean ping() {
        try (Connection connection = getConnection();
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT 1")) {
            return result.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
