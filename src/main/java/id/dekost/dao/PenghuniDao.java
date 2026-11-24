package id.dekost.dao;

import id.dekost.config.Database;
import id.dekost.model.Penghuni;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PenghuniDao {

    private static final String SELECT_COLUMNS = """
            SELECT p.id, p.user_id, p.kamar_id, k.nomor_kamar, p.nama_lengkap, p.no_hp,
                   p.pekerjaan_instansi, p.kontak_darurat, p.tanggal_masuk, p.tanggal_keluar, p.status
            FROM penghuni p
            LEFT JOIN kamar k ON k.id = p.kamar_id
            """;

    public List<Penghuni> findPage(String query, String status, int limit, long offset)
            throws SQLException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedStatus = status == null ? "" : status.trim();
        StringBuilder sql = new StringBuilder(SELECT_COLUMNS);
        appendFilters(sql, normalizedQuery, normalizedStatus);
        sql.append(" ORDER BY p.nama_lengkap LIMIT ? OFFSET ?");

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = bindFilters(statement, normalizedQuery, normalizedStatus);
            statement.setInt(parameterIndex++, limit);
            statement.setLong(parameterIndex, offset);
            try (ResultSet result = statement.executeQuery()) {
                List<Penghuni> penghuniList = new ArrayList<>();
                while (result.next()) {
                    penghuniList.add(mapRow(result));
                }
                return penghuniList;
            }
        }
    }

    public long count(String query, String status) throws SQLException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedStatus = status == null ? "" : status.trim();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM penghuni p LEFT JOIN kamar k ON k.id = p.kamar_id");
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

    public Optional<Penghuni> findById(int id) throws SQLException {
        String sql = SELECT_COLUMNS + " WHERE p.id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        }
    }

    public Optional<Assignment> findAssignment(Connection connection, int penghuniId)
            throws SQLException {
        String sql = """
                SELECT kamar_id, status, tanggal_masuk
                FROM penghuni
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, penghuniId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapAssignment(result)) : Optional.empty();
            }
        }
    }

    public Optional<Assignment> lockAssignment(Connection connection, int penghuniId)
            throws SQLException {
        String sql = """
                SELECT kamar_id, status, tanggal_masuk
                FROM penghuni
                WHERE id = ?
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, penghuniId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapAssignment(result)) : Optional.empty();
            }
        }
    }

    public Optional<String> lockRoom(Connection connection, int kamarId) throws SQLException {
        String sql = "SELECT status FROM kamar WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, kamarId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(result.getString(1)) : Optional.empty();
            }
        }
    }

    public boolean hasAssignedOccupant(Connection connection, int kamarId) throws SQLException {
        String sql = "SELECT EXISTS (SELECT 1 FROM penghuni WHERE kamar_id = ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, kamarId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    public int insertActive(Connection connection, Penghuni penghuni) throws SQLException {
        String sql = """
                INSERT INTO penghuni (
                    user_id, kamar_id, nama_lengkap, no_hp, pekerjaan_instansi,
                    kontak_darurat, tanggal_masuk, tanggal_keluar, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, NULL, 'AKTIF')
                """;
        try (PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (penghuni.getUserId() == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, penghuni.getUserId());
            }
            statement.setInt(2, penghuni.getKamarId());
            statement.setString(3, penghuni.getNamaLengkap());
            statement.setString(4, penghuni.getNoHp());
            statement.setString(5, penghuni.getPekerjaanInstansi());
            statement.setString(6, penghuni.getKontakDarurat());
            statement.setDate(7, Date.valueOf(penghuni.getTanggalMasuk()));
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Database tidak mengembalikan id penghuni.");
                }
                return generatedKeys.getInt(1);
            }
        }
    }

    public boolean updateRoomAssignment(Connection connection, int penghuniId,
                                        int oldRoomId, int newRoomId) throws SQLException {
        String sql = """
                UPDATE penghuni
                SET kamar_id = ?
                WHERE id = ? AND kamar_id = ? AND status = 'AKTIF'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newRoomId);
            statement.setInt(2, penghuniId);
            statement.setInt(3, oldRoomId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updateActiveProfile(Connection connection, Penghuni penghuni) throws SQLException {
        String sql = """
                UPDATE penghuni
                SET nama_lengkap = ?, no_hp = ?, pekerjaan_instansi = ?, kontak_darurat = ?,
                    tanggal_masuk = ?
                WHERE id = ? AND status = 'AKTIF'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, penghuni.getNamaLengkap());
            statement.setString(2, penghuni.getNoHp());
            statement.setString(3, penghuni.getPekerjaanInstansi());
            statement.setString(4, penghuni.getKontakDarurat());
            statement.setDate(5, Date.valueOf(penghuni.getTanggalMasuk()));
            statement.setInt(6, penghuni.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean checkOut(Connection connection, int penghuniId, int kamarId,
                            LocalDate tanggalKeluar) throws SQLException {
        String sql = """
                UPDATE penghuni
                SET kamar_id = NULL, tanggal_keluar = ?, status = 'NONAKTIF'
                WHERE id = ? AND kamar_id = ? AND status = 'AKTIF'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(tanggalKeluar));
            statement.setInt(2, penghuniId);
            statement.setInt(3, kamarId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean setRoomStatus(Connection connection, int kamarId, String expectedStatus,
                                 String newStatus) throws SQLException {
        String sql = "UPDATE kamar SET status = ? WHERE id = ? AND status = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus);
            statement.setInt(2, kamarId);
            statement.setString(3, expectedStatus);
            return statement.executeUpdate() == 1;
        }
    }

    private static void appendFilters(StringBuilder sql, String query, String status) {
        boolean hasFilter = false;
        if (!query.isEmpty()) {
            sql.append("""
                     WHERE (
                         p.nama_lengkap ILIKE ?
                         OR p.no_hp ILIKE ?
                         OR k.nomor_kamar ILIKE ?
                     )
                    """);
            hasFilter = true;
        }
        if (!status.isEmpty()) {
            sql.append(hasFilter ? " AND" : " WHERE").append(" p.status = ?");
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

    private static Assignment mapAssignment(ResultSet result) throws SQLException {
        int kamarIdValue = result.getInt("kamar_id");
        Integer kamarId = result.wasNull() ? null : kamarIdValue;
        Date tanggalMasuk = result.getDate("tanggal_masuk");
        return new Assignment(
                kamarId,
                result.getString("status"),
                tanggalMasuk == null ? null : tanggalMasuk.toLocalDate()
        );
    }

    private static Penghuni mapRow(ResultSet result) throws SQLException {
        int userIdValue = result.getInt("user_id");
        Integer userId = result.wasNull() ? null : userIdValue;
        int kamarIdValue = result.getInt("kamar_id");
        Integer kamarId = result.wasNull() ? null : kamarIdValue;
        Date tanggalMasuk = result.getDate("tanggal_masuk");
        Date tanggalKeluar = result.getDate("tanggal_keluar");
        return new Penghuni(
                result.getInt("id"),
                userId,
                kamarId,
                result.getString("nomor_kamar"),
                result.getString("nama_lengkap"),
                result.getString("no_hp"),
                result.getString("pekerjaan_instansi"),
                result.getString("kontak_darurat"),
                tanggalMasuk == null ? null : tanggalMasuk.toLocalDate(),
                tanggalKeluar == null ? null : tanggalKeluar.toLocalDate(),
                result.getString("status")
        );
    }

    public record Assignment(Integer kamarId, String status, LocalDate tanggalMasuk) {
    }
}
