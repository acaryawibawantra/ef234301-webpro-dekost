package id.dekost.dao;

import id.dekost.config.Database;
import id.dekost.model.Pembayaran;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PembayaranDao {

    private static final String STATUS_CASE = """
            CASE
                WHEN b.status = 'LUNAS' THEN 'LUNAS'
                WHEN b.jatuh_tempo < CURRENT_DATE THEN 'TERLAMBAT'
                ELSE 'BELUM_BAYAR'
            END
            """;

    private static final String FROM_CLAUSE = """
            FROM pembayaran b
            JOIN penghuni p ON p.id = b.penghuni_id
            LEFT JOIN kamar k ON k.id = p.kamar_id
            """;

    private static final String SELECT_COLUMNS = """
            SELECT b.id, b.penghuni_id, p.nama_lengkap AS penghuni_nama,
                   k.nomor_kamar, b.periode, b.jumlah, b.jatuh_tempo,
                   b.tanggal_bayar, b.metode,
            """ + STATUS_CASE + " AS status, b.bukti_url " + FROM_CLAUSE;

    public List<Pembayaran> findPage(String query, String status, int limit, long offset)
            throws SQLException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedStatus = status == null ? "" : status.trim();
        StringBuilder sql = new StringBuilder(SELECT_COLUMNS);
        appendFilters(sql, normalizedQuery, normalizedStatus);
        sql.append(" ORDER BY b.periode DESC, p.nama_lengkap, b.id DESC LIMIT ? OFFSET ?");

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = bindFilters(statement, normalizedQuery, normalizedStatus);
            statement.setInt(parameterIndex++, limit);
            statement.setLong(parameterIndex, offset);
            try (ResultSet result = statement.executeQuery()) {
                List<Pembayaran> pembayaranList = new ArrayList<>();
                while (result.next()) {
                    pembayaranList.add(mapRow(result));
                }
                return pembayaranList;
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

    public Optional<Pembayaran> findById(int id) throws SQLException {
        String sql = SELECT_COLUMNS + " WHERE b.id = ?";
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
                SELECT p.id, p.nama_lengkap, k.nomor_kamar, tk.harga_bulanan, p.status
                FROM penghuni p
                LEFT JOIN kamar k ON k.id = p.kamar_id
                LEFT JOIN tipe_kamar tk ON tk.id = k.tipe_kamar_id
                ORDER BY p.nama_lengkap
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<OccupantOption> occupants = new ArrayList<>();
            while (result.next()) {
                occupants.add(new OccupantOption(
                        result.getInt("id"),
                        result.getString("nama_lengkap"),
                        result.getString("nomor_kamar"),
                        result.getBigDecimal("harga_bulanan"),
                        result.getString("status")
                ));
            }
            return occupants;
        }
    }

    public Optional<BigDecimal> findCurrentRent(int penghuniId) throws SQLException {
        String sql = """
                SELECT tk.harga_bulanan
                FROM penghuni p
                JOIN kamar k ON k.id = p.kamar_id
                JOIN tipe_kamar tk ON tk.id = k.tipe_kamar_id
                WHERE p.id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, penghuniId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? Optional.of(result.getBigDecimal("harga_bulanan"))
                        : Optional.empty();
            }
        }
    }

    public int create(Pembayaran pembayaran) throws SQLException {
        String sql = """
                INSERT INTO pembayaran (
                    penghuni_id, periode, jumlah, jatuh_tempo, status, bukti_url
                ) VALUES (?, ?, ?, ?, 'BELUM_BAYAR', ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindEditableFields(statement, pembayaran);
            statement.setString(5, pembayaran.getBuktiUrl());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Database tidak mengembalikan id pembayaran.");
                }
                return generatedKeys.getInt(1);
            }
        }
    }

    public boolean update(Pembayaran pembayaran) throws SQLException {
        String sql = """
                UPDATE pembayaran
                SET penghuni_id = ?, periode = ?, jumlah = ?, jatuh_tempo = ?, bukti_url = ?
                WHERE id = ? AND status <> 'LUNAS'
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindEditableFields(statement, pembayaran);
            statement.setString(5, pembayaran.getBuktiUrl());
            statement.setInt(6, pembayaran.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean markPaid(int id, Date tanggalBayar, String metode) throws SQLException {
        String sql = """
                UPDATE pembayaran
                SET status = 'LUNAS', tanggal_bayar = ?, metode = ?
                WHERE id = ? AND status <> 'LUNAS'
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, tanggalBayar);
            statement.setString(2, metode);
            statement.setInt(3, id);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM pembayaran WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    public int generateForCurrentMonth() throws SQLException {
        String sql = """
                WITH current_month AS (
                    SELECT DATE_TRUNC('month', CURRENT_DATE)::date AS periode
                )
                INSERT INTO pembayaran (penghuni_id, periode, jumlah, jatuh_tempo, status)
                SELECT p.id,
                       current_month.periode,
                       tk.harga_bulanan,
                       current_month.periode + 4,
                       'BELUM_BAYAR'
                FROM penghuni p
                JOIN kamar k ON k.id = p.kamar_id
                JOIN tipe_kamar tk ON tk.id = k.tipe_kamar_id
                CROSS JOIN current_month
                WHERE p.status = ?
                  AND p.kamar_id IS NOT NULL
                  AND NOT EXISTS (
                      SELECT 1
                      FROM pembayaran existing
                      WHERE existing.penghuni_id = p.id
                        AND existing.periode = current_month.periode
                  )
                ON CONFLICT (penghuni_id, periode) DO NOTHING
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "AKTIF");
            return statement.executeUpdate();
        }
    }

    private static void appendFilters(StringBuilder sql, String query, String status) {
        boolean hasFilter = false;
        if (!query.isEmpty()) {
            sql.append("""
                     WHERE (
                         p.nama_lengkap ILIKE ?
                         OR COALESCE(k.nomor_kamar, '') ILIKE ?
                         OR CAST(b.periode AS TEXT) ILIKE ?
                     )
                    """);
            hasFilter = true;
        }
        if (!status.isEmpty()) {
            sql.append(hasFilter ? " AND " : " WHERE ")
                    .append('(').append(STATUS_CASE).append(") = ?");
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

    private static void bindEditableFields(PreparedStatement statement, Pembayaran pembayaran)
            throws SQLException {
        statement.setInt(1, pembayaran.getPenghuniId());
        statement.setDate(2, Date.valueOf(pembayaran.getPeriode()));
        BigDecimal jumlah = pembayaran.getJumlah();
        statement.setBigDecimal(3, jumlah);
        statement.setDate(4, Date.valueOf(pembayaran.getJatuhTempo()));
    }

    private static Pembayaran mapRow(ResultSet result) throws SQLException {
        Date periode = result.getDate("periode");
        Date jatuhTempo = result.getDate("jatuh_tempo");
        Date tanggalBayar = result.getDate("tanggal_bayar");
        Pembayaran pembayaran = new Pembayaran(
                result.getInt("id"),
                result.getInt("penghuni_id"),
                periode == null ? null : periode.toLocalDate(),
                result.getBigDecimal("jumlah"),
                jatuhTempo == null ? null : jatuhTempo.toLocalDate(),
                tanggalBayar == null ? null : tanggalBayar.toLocalDate(),
                result.getString("metode"),
                result.getString("status"),
                result.getString("bukti_url")
        );
        pembayaran.setPenghuniNama(result.getString("penghuni_nama"));
        pembayaran.setNomorKamar(result.getString("nomor_kamar"));
        return pembayaran;
    }

    public record OccupantOption(int id, String nama, String nomorKamar, BigDecimal hargaBulanan,
                                 String status) {
        public int getId() {
            return id;
        }

        public String getNama() {
            return nama;
        }

        public String getNomorKamar() {
            return nomorKamar;
        }

        public BigDecimal getHargaBulanan() {
            return hargaBulanan;
        }

        public String getStatus() {
            return status;
        }
    }
}
