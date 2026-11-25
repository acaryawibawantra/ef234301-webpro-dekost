package id.dekost.dao;

import id.dekost.config.Database;
import id.dekost.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final String SELECT_ACCOUNT = """
            SELECT u.id, u.nama, u.email, u.role, u.created_at,
                   p.id AS penghuni_id, p.nama_lengkap AS penghuni_nama, k.nomor_kamar
            FROM users u
            LEFT JOIN penghuni p ON p.user_id = u.id
            LEFT JOIN kamar k ON k.id = p.kamar_id
            """;

    private static final String FIND_BY_EMAIL = """
            SELECT id, nama, email, password_hash, role, created_at
            FROM users
            WHERE email = ?
            """;

    public Optional<User> findByEmail(String email) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL)) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                Timestamp createdAt = result.getTimestamp("created_at");
                User user = new User(
                        result.getInt("id"),
                        result.getString("nama"),
                        result.getString("email"),
                        result.getString("password_hash"),
                        result.getString("role"),
                        createdAt == null ? null : createdAt.toLocalDateTime()
                );
                return Optional.of(user);
            }
        }
    }

    public List<AccountRow> findPage(String query, String role, int limit, long offset)
            throws SQLException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedRole = role == null ? "" : role.trim();
        StringBuilder sql = new StringBuilder(SELECT_ACCOUNT);
        appendFilters(sql, normalizedQuery, normalizedRole);
        sql.append(" ORDER BY CASE WHEN u.role = 'ADMIN' THEN 0 ELSE 1 END, u.nama LIMIT ? OFFSET ?");

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = bindFilters(statement, normalizedQuery, normalizedRole);
            statement.setInt(parameterIndex++, limit);
            statement.setLong(parameterIndex, offset);
            try (ResultSet result = statement.executeQuery()) {
                List<AccountRow> accounts = new ArrayList<>();
                while (result.next()) {
                    accounts.add(mapAccount(result));
                }
                return accounts;
            }
        }
    }

    public long count(String query, String role) throws SQLException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedRole = role == null ? "" : role.trim();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users u");
        appendFilters(sql, normalizedQuery, normalizedRole);

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindFilters(statement, normalizedQuery, normalizedRole);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    public Optional<AccountRow> findById(int id) throws SQLException {
        String sql = SELECT_ACCOUNT + " WHERE u.id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapAccount(result)) : Optional.empty();
            }
        }
    }

    public boolean existsByEmail(String email, Integer excludedId) throws SQLException {
        String sql = excludedId == null
                ? "SELECT EXISTS (SELECT 1 FROM users WHERE LOWER(email) = LOWER(?))"
                : "SELECT EXISTS (SELECT 1 FROM users WHERE LOWER(email) = LOWER(?) AND id <> ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            if (excludedId != null) {
                statement.setInt(2, excludedId);
            }
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    public int create(User user, String passwordHash) throws SQLException {
        String sql = """
                INSERT INTO users (nama, email, password_hash, role)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getNama());
            statement.setString(2, user.getEmail());
            statement.setString(3, passwordHash);
            statement.setString(4, user.getRole());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Database tidak mengembalikan id akun.");
                }
                return generatedKeys.getInt(1);
            }
        }
    }

    public boolean update(User user) throws SQLException {
        String sql = """
                UPDATE users
                SET nama = ?, email = ?, role = ?
                WHERE id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getNama());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getRole());
            statement.setInt(4, user.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updatePassword(int id, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setInt(2, id);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    public long countByRole(String role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    public boolean linkPenghuni(int userId, int penghuniId) throws SQLException {
        String sql = "UPDATE penghuni SET user_id = ? WHERE id = ? AND user_id IS NULL";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, penghuniId);
            return statement.executeUpdate() == 1;
        }
    }

    private static void appendFilters(StringBuilder sql, String query, String role) {
        boolean hasFilter = false;
        if (!query.isEmpty()) {
            sql.append(" WHERE (u.nama ILIKE ? OR u.email ILIKE ?)");
            hasFilter = true;
        }
        if (!role.isEmpty()) {
            sql.append(hasFilter ? " AND" : " WHERE").append(" u.role = ?");
        }
    }

    private static int bindFilters(PreparedStatement statement, String query, String role)
            throws SQLException {
        int parameterIndex = 1;
        if (!query.isEmpty()) {
            String pattern = "%" + query + "%";
            statement.setString(parameterIndex++, pattern);
            statement.setString(parameterIndex++, pattern);
        }
        if (!role.isEmpty()) {
            statement.setString(parameterIndex++, role);
        }
        return parameterIndex;
    }

    private static AccountRow mapAccount(ResultSet result) throws SQLException {
        Timestamp createdAt = result.getTimestamp("created_at");
        int penghuniIdValue = result.getInt("penghuni_id");
        Integer penghuniId = result.wasNull() ? null : penghuniIdValue;
        return new AccountRow(
                result.getInt("id"),
                result.getString("nama"),
                result.getString("email"),
                result.getString("role"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                penghuniId,
                result.getString("penghuni_nama"),
                result.getString("nomor_kamar")
        );
    }

    public record AccountRow(
            int id,
            String nama,
            String email,
            String role,
            LocalDateTime createdAt,
            Integer penghuniId,
            String penghuniNama,
            String nomorKamar
    ) {
        public int getId() {
            return id;
        }

        public String getNama() {
            return nama;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public Integer getPenghuniId() {
            return penghuniId;
        }

        public String getPenghuniNama() {
            return penghuniNama;
        }

        public String getNomorKamar() {
            return nomorKamar;
        }
    }
}
