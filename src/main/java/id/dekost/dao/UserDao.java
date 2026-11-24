package id.dekost.dao;

import id.dekost.config.Database;
import id.dekost.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class UserDao {

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
}
