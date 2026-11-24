package id.dekost.dao;

import id.dekost.config.Database;
import id.dekost.model.TipeKamar;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TipeKamarDao {

    private static final String SELECT_COLUMNS =
            "SELECT id, nama, harga_bulanan, luas_m2, fasilitas FROM tipe_kamar";

    public List<TipeKamar> findPage(String query, int limit, long offset) throws SQLException {
        String normalizedQuery = query == null ? "" : query.trim();
        String sql = normalizedQuery.isEmpty()
                ? SELECT_COLUMNS + " ORDER BY nama LIMIT ? OFFSET ?"
                : SELECT_COLUMNS + " WHERE nama ILIKE ? ORDER BY nama LIMIT ? OFFSET ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            if (!normalizedQuery.isEmpty()) {
                statement.setString(parameterIndex++, "%" + normalizedQuery + "%");
            }
            statement.setInt(parameterIndex++, limit);
            statement.setLong(parameterIndex, offset);

            try (ResultSet result = statement.executeQuery()) {
                List<TipeKamar> tipeKamarList = new ArrayList<>();
                while (result.next()) {
                    tipeKamarList.add(mapRow(result));
                }
                return tipeKamarList;
            }
        }
    }

    public List<TipeKamar> findAll() throws SQLException {
        String sql = SELECT_COLUMNS + " ORDER BY nama";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<TipeKamar> tipeKamarList = new ArrayList<>();
            while (result.next()) {
                tipeKamarList.add(mapRow(result));
            }
            return tipeKamarList;
        }
    }

    public long count(String query) throws SQLException {
        String normalizedQuery = query == null ? "" : query.trim();
        String sql = normalizedQuery.isEmpty()
                ? "SELECT COUNT(*) FROM tipe_kamar"
                : "SELECT COUNT(*) FROM tipe_kamar WHERE nama ILIKE ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (!normalizedQuery.isEmpty()) {
                statement.setString(1, "%" + normalizedQuery + "%");
            }

            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    public Optional<TipeKamar> findById(int id) throws SQLException {
        String sql = SELECT_COLUMNS + " WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        }
    }

    public boolean existsByNama(String nama, Integer excludedId) throws SQLException {
        String sql = excludedId == null
                ? "SELECT EXISTS (SELECT 1 FROM tipe_kamar WHERE nama = ?)"
                : "SELECT EXISTS (SELECT 1 FROM tipe_kamar WHERE nama = ? AND id <> ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nama);
            if (excludedId != null) {
                statement.setInt(2, excludedId);
            }
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    public int create(TipeKamar tipeKamar) throws SQLException {
        String sql = """
                INSERT INTO tipe_kamar (nama, harga_bulanan, luas_m2, fasilitas)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindFields(statement, tipeKamar);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Database tidak mengembalikan id tipe kamar.");
                }
                return generatedKeys.getInt(1);
            }
        }
    }

    public boolean update(TipeKamar tipeKamar) throws SQLException {
        String sql = """
                UPDATE tipe_kamar
                SET nama = ?, harga_bulanan = ?, luas_m2 = ?, fasilitas = ?
                WHERE id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindFields(statement, tipeKamar);
            statement.setInt(5, tipeKamar.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public DeleteResult deleteById(int id) throws SQLException {
        String sql = "DELETE FROM tipe_kamar WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try {
                return statement.executeUpdate() == 1
                        ? DeleteResult.DELETED
                        : DeleteResult.NOT_FOUND;
            } catch (SQLException e) {
                if ("23503".equals(e.getSQLState())) {
                    return DeleteResult.IN_USE;
                }
                throw e;
            }
        }
    }

    private static void bindFields(PreparedStatement statement, TipeKamar tipeKamar)
            throws SQLException {
        statement.setString(1, tipeKamar.getNama());
        BigDecimal hargaBulanan = tipeKamar.getHargaBulanan();
        statement.setBigDecimal(2, hargaBulanan);
        statement.setInt(3, tipeKamar.getLuasM2());
        statement.setString(4, tipeKamar.getFasilitas());
    }

    private static TipeKamar mapRow(ResultSet result) throws SQLException {
        return new TipeKamar(
                result.getInt("id"),
                result.getString("nama"),
                result.getBigDecimal("harga_bulanan"),
                result.getInt("luas_m2"),
                result.getString("fasilitas")
        );
    }

    public enum DeleteResult {
        DELETED,
        NOT_FOUND,
        IN_USE
    }
}
