package id.dekost.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import id.dekost.util.Validator;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

@WebListener
public class DatabaseInitializer implements ServletContextListener {

    private static final long BOOTSTRAP_LOCK_ID = 7_289_347_193L;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                acquireBootstrapLock(connection);
                if (!hasSchema(connection)) {
                    createSchema(connection);
                }
                if (!hasAdmin(connection)) {
                    createAdmin(connection);
                }
                connection.commit();
            } catch (SQLException | IOException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw new IllegalStateException("Gagal menyiapkan database aplikasi.", e);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal terhubung ke database aplikasi.", e);
        }
    }

    private static void acquireBootstrapLock(Connection connection) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT pg_advisory_xact_lock(?)")) {
            statement.setLong(1, BOOTSTRAP_LOCK_ID);
            statement.executeQuery().close();
        }
    }

    private static boolean hasSchema(Connection connection) throws SQLException {
        String sql = """
                SELECT to_regclass('public.users') IS NOT NULL
                   AND to_regclass('public.tipe_kamar') IS NOT NULL
                   AND to_regclass('public.kamar') IS NOT NULL
                   AND to_regclass('public.penghuni') IS NOT NULL
                   AND to_regclass('public.pembayaran') IS NOT NULL
                   AND to_regclass('public.keluhan') IS NOT NULL
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            return result.next() && result.getBoolean(1);
        }
    }

    private static void createSchema(Connection connection) throws SQLException, IOException {
        try (InputStream input = DatabaseInitializer.class.getResourceAsStream("/db/schema.sql")) {
            if (input == null) {
                throw new IOException("Resource schema database tidak ditemukan.");
            }

            String schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            for (String sql : schema.split(";")) {
                if (sql.isBlank()) {
                    continue;
                }
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.execute();
                }
            }
        }
    }

    private static boolean hasAdmin(Connection connection) throws SQLException {
        String sql = "SELECT EXISTS (SELECT 1 FROM users WHERE role = 'ADMIN')";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            return result.next() && result.getBoolean(1);
        }
    }

    private static void createAdmin(Connection connection) throws SQLException {
        String email = requiredEnv("ADMIN_EMAIL").trim().toLowerCase(Locale.ROOT);
        String password = requiredEnv("ADMIN_PASSWORD");
        if (!Validator.isValidEmail(email)) {
            throw new IllegalStateException("ADMIN_EMAIL harus berupa alamat email yang valid.");
        }
        int passwordBytes = password.getBytes(StandardCharsets.UTF_8).length;
        if (password.length() < 8 || password.length() > 72 || passwordBytes > 72) {
            throw new IllegalStateException(
                    "ADMIN_PASSWORD harus 8-72 karakter dan maksimal 72 byte UTF-8.");
        }

        char[] passwordChars = password.toCharArray();
        String passwordHash;
        try {
            passwordHash = BCrypt.withDefaults().hashToString(12, passwordChars);
        } finally {
            Arrays.fill(passwordChars, '\0');
        }

        String sql = """
                INSERT INTO users (nama, email, password_hash, role)
                VALUES (?, ?, ?, 'ADMIN')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "Pengelola Kos");
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.executeUpdate();
        }
    }

    private static String requiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable " + key + " harus diatur.");
        }
        return value;
    }
}
