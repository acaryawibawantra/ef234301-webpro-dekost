package id.dekost.dao;

import id.dekost.config.Database;
import id.dekost.model.Kamar;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KamarDao {

    private static final String SELECT_COLUMNS = """
            SELECT k.id, k.nomor_kamar, k.lantai, k.tipe_kamar_id,
                   tk.nama AS tipe_kamar_nama, k.status, k.catatan
            FROM kamar k
            JOIN tipe_kamar tk ON tk.id = k.tipe_kamar_id
            """;

    public List<Kamar> findPage(String query, String status, int limit, long offset)
            throws SQLException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedStatus = status == null ? "" : status.trim();
        StringBuilder sql = new StringBuilder(SELECT_COLUMNS);
        appendFilters(sql, normalizedQuery, normalizedStatus);
        sql.append(" ORDER BY k.nomor_kamar LIMIT ? OFFSET ?");

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = bindFilters(statement, normalizedQuery, normalizedStatus);
            statement.setInt(parameterIndex++, limit);
            statement.setLong(parameterIndex, offset);

            try (ResultSet result = statement.executeQuery()) {
                List<Kamar> kamarList = new ArrayList<>();
                while (result.next()) {
                    kamarList.add(mapRow(result));
                }
                return kamarList;
            }
        }
    }

    public long count(String query, String status) throws SQLException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedStatus = status == null ? "" : status.trim();
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM kamar k JOIN tipe_kamar tk ON tk.id = k.tipe_kamar_id");
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

    public Optional<Kamar> findById(int id) throws SQLException {
        String sql = SELECT_COLUMNS + " WHERE k.id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        }
    }

    public List<Kamar> findAvailableRooms() throws SQLException {
        String sql = SELECT_COLUMNS + """
                 WHERE k.status = 'TERSEDIA'
                   AND NOT EXISTS (
                       SELECT 1 FROM penghuni p WHERE p.kamar_id = k.id
                   )
                 ORDER BY k.nomor_kamar
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Kamar> kamarList = new ArrayList<>();
            while (result.next()) {
                kamarList.add(mapRow(result));
            }
            return kamarList;
        }
    }

    public List<TypeAvailability> findAvailabilityByType() throws SQLException {
        String sql = """
                SELECT tk.id, tk.nama, tk.harga_bulanan, tk.luas_m2, tk.fasilitas,
                       COUNT(k.id) AS total_kamar,
                       COUNT(k.id) FILTER (
                           WHERE k.status = 'TERSEDIA'
                             AND NOT EXISTS (
                                 SELECT 1 FROM penghuni p
                                 WHERE p.kamar_id = k.id AND p.status = 'AKTIF'
                             )
                       ) AS kamar_tersedia,
                       COUNT(k.id) FILTER (WHERE k.status = 'TERISI') AS kamar_terisi
                FROM tipe_kamar tk
                LEFT JOIN kamar k ON k.tipe_kamar_id = tk.id
                GROUP BY tk.id, tk.nama, tk.harga_bulanan, tk.luas_m2, tk.fasilitas
                ORDER BY tk.harga_bulanan, tk.nama
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<TypeAvailability> availability = new ArrayList<>();
            while (result.next()) {
                availability.add(new TypeAvailability(
                        result.getInt("id"),
                        result.getString("nama"),
                        result.getBigDecimal("harga_bulanan"),
                        result.getInt("luas_m2"),
                        result.getString("fasilitas"),
                        result.getLong("total_kamar"),
                        result.getLong("kamar_tersedia"),
                        result.getLong("kamar_terisi")
                ));
            }
            return availability;
        }
    }

    public boolean existsByNomorKamar(String nomorKamar, Integer excludedId) throws SQLException {
        String sql = excludedId == null
                ? "SELECT EXISTS (SELECT 1 FROM kamar WHERE nomor_kamar = ?)"
                : "SELECT EXISTS (SELECT 1 FROM kamar WHERE nomor_kamar = ? AND id <> ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nomorKamar);
            if (excludedId != null) {
                statement.setInt(2, excludedId);
            }
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    public boolean existsTipeKamar(int tipeKamarId) throws SQLException {
        String sql = "SELECT EXISTS (SELECT 1 FROM tipe_kamar WHERE id = ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, tipeKamarId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    public int create(Kamar kamar, String initialStatus) throws SQLException {
        if (!isAllowedInitialStatus(initialStatus)) {
            throw new IllegalArgumentException("Status awal kamar tidak valid.");
        }

        String sql = """
                INSERT INTO kamar (nomor_kamar, lantai, tipe_kamar_id, status, catatan)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindRoomFields(statement, kamar);
            statement.setString(4, initialStatus);
            statement.setString(5, kamar.getCatatan());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Database tidak mengembalikan id kamar.");
                }
                return generatedKeys.getInt(1);
            }
        }
    }

    public UpdateResult update(Kamar kamar, String requestedStatus) throws SQLException {
        if (requestedStatus != null && !isManualStatus(requestedStatus)) {
            return UpdateResult.INVALID_STATUS;
        }

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String currentStatus = lockStatus(connection, kamar.getId());
                if (currentStatus == null) {
                    connection.rollback();
                    return UpdateResult.NOT_FOUND;
                }

                if (requestedStatus != null) {
                    if ("TERISI".equals(currentStatus)) {
                        connection.rollback();
                        return UpdateResult.STATUS_MANAGED_BY_OCCUPANT;
                    }
                    if (hasActiveOccupant(connection, kamar.getId())) {
                        connection.rollback();
                        return UpdateResult.ACTIVE_OCCUPANT;
                    }
                }

                String sql = requestedStatus == null
                        ? """
                          UPDATE kamar
                          SET nomor_kamar = ?, lantai = ?, tipe_kamar_id = ?, catatan = ?
                          WHERE id = ?
                          """
                        : """
                          UPDATE kamar
                          SET nomor_kamar = ?, lantai = ?, tipe_kamar_id = ?, catatan = ?, status = ?
                          WHERE id = ?
                          """;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    bindRoomFields(statement, kamar);
                    statement.setString(4, kamar.getCatatan());
                    int idParameterIndex = 5;
                    if (requestedStatus != null) {
                        statement.setString(5, requestedStatus);
                        idParameterIndex = 6;
                    }
                    statement.setInt(idParameterIndex, kamar.getId());
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

    public DeleteResult deleteById(int id) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (lockStatus(connection, id) == null) {
                    connection.rollback();
                    return DeleteResult.NOT_FOUND;
                }
                if (hasActiveOccupant(connection, id)) {
                    connection.rollback();
                    return DeleteResult.ACTIVE_OCCUPANT;
                }

                String sql = "DELETE FROM kamar WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, id);
                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return DeleteResult.NOT_FOUND;
                    }
                }

                connection.commit();
                return DeleteResult.DELETED;
            } catch (SQLException e) {
                rollback(connection, e);
                throw e;
            }
        }
    }

    private static void appendFilters(StringBuilder sql, String query, String status) {
        boolean hasFilter = false;
        if (!query.isEmpty()) {
            sql.append(" WHERE (k.nomor_kamar ILIKE ? OR tk.nama ILIKE ?)");
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
        }
        if (!status.isEmpty()) {
            statement.setString(parameterIndex++, status);
        }
        return parameterIndex;
    }

    private static void bindRoomFields(PreparedStatement statement, Kamar kamar) throws SQLException {
        statement.setString(1, kamar.getNomorKamar());
        statement.setInt(2, kamar.getLantai());
        statement.setInt(3, kamar.getTipeKamarId());
    }

    private static String lockStatus(Connection connection, int id) throws SQLException {
        String sql = "SELECT status FROM kamar WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : null;
            }
        }
    }

    private static boolean hasActiveOccupant(Connection connection, int kamarId) throws SQLException {
        String sql = "SELECT EXISTS (SELECT 1 FROM penghuni WHERE kamar_id = ? AND status = 'AKTIF')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, kamarId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    private static boolean isAllowedInitialStatus(String status) {
        return "TERSEDIA".equals(status) || "PERBAIKAN".equals(status);
    }

    private static boolean isManualStatus(String status) {
        return "TERSEDIA".equals(status) || "PERBAIKAN".equals(status);
    }

    private static Kamar mapRow(ResultSet result) throws SQLException {
        return new Kamar(
                result.getInt("id"),
                result.getString("nomor_kamar"),
                result.getInt("lantai"),
                result.getInt("tipe_kamar_id"),
                result.getString("tipe_kamar_nama"),
                result.getString("status"),
                result.getString("catatan")
        );
    }

    private static void rollback(Connection connection, SQLException originalError) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            originalError.addSuppressed(rollbackError);
        }
    }

    public record TypeAvailability(
            int tipeKamarId,
            String nama,
            BigDecimal hargaBulanan,
            int luasM2,
            String fasilitas,
            long totalKamar,
            long kamarTersedia,
            long kamarTerisi
    ) {
        public int getTipeKamarId() {
            return tipeKamarId;
        }

        public String getNama() {
            return nama;
        }

        public BigDecimal getHargaBulanan() {
            return hargaBulanan;
        }

        public int getLuasM2() {
            return luasM2;
        }

        public String getFasilitas() {
            return fasilitas;
        }

        public long getTotalKamar() {
            return totalKamar;
        }

        public long getKamarTersedia() {
            return kamarTersedia;
        }

        public long getKamarTerisi() {
            return kamarTerisi;
        }

        public boolean isPenuh() {
            return kamarTersedia <= 0;
        }
    }

    public enum UpdateResult {
        UPDATED,
        NOT_FOUND,
        INVALID_STATUS,
        STATUS_MANAGED_BY_OCCUPANT,
        ACTIVE_OCCUPANT
    }

    public enum DeleteResult {
        DELETED,
        NOT_FOUND,
        ACTIVE_OCCUPANT
    }
}
