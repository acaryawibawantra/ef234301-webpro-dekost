package id.dekost.dao;

import id.dekost.config.Database;
import id.dekost.model.Keluhan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KeluhanDao {

    private static final String FROM_CLAUSE = """
            FROM keluhan k
            JOIN penghuni p ON p.id = k.penghuni_id
            LEFT JOIN users u ON u.id = k.ditangani_oleh
            """;

    private static final String SELECT_COLUMNS = """
            SELECT k.id, k.penghuni_id, p.nama_lengkap AS penghuni_nama,
                   k.ditangani_oleh, u.nama AS penangan_nama, k.judul, k.deskripsi,
                   k.kategori, k.prioritas, k.status, k.tanggapan,
                   k.created_at, k.resolved_at
            """ + FROM_CLAUSE;

    public List<Keluhan> findPage(String query, String status, int limit, long offset)
            throws SQLException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedStatus = status == null ? "" : status.trim();
        StringBuilder sql = new StringBuilder(SELECT_COLUMNS);
        appendFilters(sql, normalizedQuery, normalizedStatus);
        sql.append(" ORDER BY k.created_at DESC, k.id DESC LIMIT ? OFFSET ?");

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = bindFilters(statement, normalizedQuery, normalizedStatus);
            statement.setInt(parameterIndex++, limit);
            statement.setLong(parameterIndex, offset);
            try (ResultSet result = statement.executeQuery()) {
                List<Keluhan> keluhanList = new ArrayList<>();
                while (result.next()) {
                    keluhanList.add(mapRow(result));
                }
                return keluhanList;
            }
        }
    }

    public long count(String query, String status) throws SQLException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedStatus = status == null ? "" : status.trim();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) ").append(FROM_CLAUSE);
        appendFilters(sql, normalizedQuery, normalizedStatus);

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindFilters(statement, normalizedQuery, normalizedStatus);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    public Optional<Keluhan> findById(int id) throws SQLException {
        String sql = SELECT_COLUMNS + " WHERE k.id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        }
    }

    public List<OccupantOption> findOccupantOptions() throws SQLException {
        String sql = """
                SELECT id, nama_lengkap, status
                FROM penghuni
                ORDER BY nama_lengkap
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<OccupantOption> occupants = new ArrayList<>();
            while (result.next()) {
                occupants.add(new OccupantOption(
                        result.getInt("id"),
                        result.getString("nama_lengkap"),
                        result.getString("status")
                ));
            }
            return occupants;
        }
    }

    public int create(Keluhan keluhan) throws SQLException {
        String sql = """
                INSERT INTO keluhan (
                    penghuni_id, judul, deskripsi, kategori, prioritas, status, tanggapan
                ) VALUES (?, ?, ?, ?, ?, 'BARU', ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, keluhan.getPenghuniId());
            statement.setString(2, keluhan.getJudul());
            statement.setString(3, keluhan.getDeskripsi());
            statement.setString(4, keluhan.getKategori());
            statement.setString(5, keluhan.getPrioritas());
            statement.setString(6, keluhan.getTanggapan());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Database tidak mengembalikan id keluhan.");
                }
                return generatedKeys.getInt(1);
            }
        }
    }

    public UpdateResult update(Keluhan keluhan, Integer adminId) throws SQLException {
        if (!isValidStatus(keluhan.getStatus())) {
            return UpdateResult.INVALID_STATUS;
        }
        if ("DIPROSES".equals(keluhan.getStatus()) && (adminId == null || adminId < 1)) {
            return UpdateResult.INVALID_HANDLER;
        }

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String currentStatus = lockStatus(connection, keluhan.getId());
                if (currentStatus == null) {
                    connection.rollback();
                    return UpdateResult.NOT_FOUND;
                }
                if ("SELESAI".equals(currentStatus) && "BARU".equals(keluhan.getStatus())) {
                    connection.rollback();
                    return UpdateResult.CANNOT_REOPEN_TO_NEW;
                }

                String sql = """
                        UPDATE keluhan
                        SET penghuni_id = ?, judul = ?, deskripsi = ?, kategori = ?, prioritas = ?,
                            status = ?, tanggapan = ?,
                            ditangani_oleh = CASE
                                WHEN ? = 'DIPROSES' AND ? <> 'DIPROSES' THEN ?
                                WHEN ? = 'BARU' THEN NULL
                                ELSE ditangani_oleh
                            END,
                            resolved_at = CASE
                                WHEN ? = 'SELESAI' THEN COALESCE(resolved_at, CURRENT_TIMESTAMP)
                                ELSE NULL
                            END
                        WHERE id = ?
                        """;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, keluhan.getPenghuniId());
                    statement.setString(2, keluhan.getJudul());
                    statement.setString(3, keluhan.getDeskripsi());
                    statement.setString(4, keluhan.getKategori());
                    statement.setString(5, keluhan.getPrioritas());
                    statement.setString(6, keluhan.getStatus());
                    statement.setString(7, keluhan.getTanggapan());
                    statement.setString(8, keluhan.getStatus());
                    statement.setString(9, currentStatus);
                    if (adminId == null) {
                        statement.setNull(10, Types.INTEGER);
                    } else {
                        statement.setInt(10, adminId);
                    }
                    statement.setString(11, keluhan.getStatus());
                    statement.setString(12, keluhan.getStatus());
                    statement.setInt(13, keluhan.getId());
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return UpdateResult.NOT_FOUND;
                    }
                }
                connection.commit();
                return UpdateResult.UPDATED;
            } catch (SQLException e) {
                rollback(connection, e);
                throw e;
            }
        }
    }

    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM keluhan WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private static void appendFilters(StringBuilder sql, String query, String status) {
        boolean hasFilter = false;
        if (!query.isEmpty()) {
            sql.append("""
                     WHERE (
                         k.judul ILIKE ?
                         OR k.deskripsi ILIKE ?
                         OR p.nama_lengkap ILIKE ?
                     )
                    """);
            hasFilter = true;
        }
        if (!status.isEmpty()) {
            sql.append(hasFilter ? " AND" : " WHERE").append(" k.status = ?");
        }
    }

    private static int bindFilters(PreparedStatement statement, String query, String status)
            throws SQLException {
        int parameterIndex = 1;
        if (!query.isEmpty()) {
            String pattern = "%" + query + "%";
            statement.setString(parameterIndex++, pattern);
            statement.setString(parameterIndex++, pattern);
            statement.setString(parameterIndex++, pattern);
        }
        if (!status.isEmpty()) {
            statement.setString(parameterIndex++, status);
        }
        return parameterIndex;
    }

    private static String lockStatus(Connection connection, int id) throws SQLException {
        String sql = "SELECT status FROM keluhan WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("status") : null;
            }
        }
    }

    private static boolean isValidStatus(String status) {
        return "BARU".equals(status) || "DIPROSES".equals(status) || "SELESAI".equals(status);
    }

    private static void rollback(Connection connection, SQLException originalError) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            originalError.addSuppressed(rollbackError);
        }
    }

    private static Keluhan mapRow(ResultSet result) throws SQLException {
        int handlerValue = result.getInt("ditangani_oleh");
        Integer handlerId = result.wasNull() ? null : handlerValue;
        Timestamp createdAt = result.getTimestamp("created_at");
        Timestamp resolvedAt = result.getTimestamp("resolved_at");
        Keluhan keluhan = new Keluhan(
                result.getInt("id"),
                result.getInt("penghuni_id"),
                handlerId,
                result.getString("judul"),
                result.getString("deskripsi"),
                result.getString("kategori"),
                result.getString("prioritas"),
                result.getString("status"),
                result.getString("tanggapan"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                resolvedAt == null ? null : resolvedAt.toLocalDateTime()
        );
        keluhan.setPenghuniNama(result.getString("penghuni_nama"));
        keluhan.setPenanganNama(result.getString("penangan_nama"));
        return keluhan;
    }

    public enum UpdateResult {
        UPDATED,
        NOT_FOUND,
        INVALID_STATUS,
        INVALID_HANDLER,
        CANNOT_REOPEN_TO_NEW
    }

    public record OccupantOption(int id, String namaLengkap, String status) {
        public int getId() {
            return id;
        }

        public String getNamaLengkap() {
            return namaLengkap;
        }

        public String getStatus() {
            return status;
        }
    }
}
