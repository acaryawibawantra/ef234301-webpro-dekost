package id.dekost.dao;

import id.dekost.config.Database;
import id.dekost.model.Keluhan;
import id.dekost.model.Pembayaran;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PortalDao {

    private static final String PAYMENT_STATUS_CASE = """
            CASE
                WHEN b.status = 'LUNAS' THEN 'LUNAS'
                WHEN b.jatuh_tempo < CURRENT_DATE THEN 'TERLAMBAT'
                ELSE 'BELUM_BAYAR'
            END
            """;

    private static final String SUMMARY_SQL = "SELECT "
            + "COUNT(*) FILTER (WHERE " + PAYMENT_STATUS_CASE + " <> 'LUNAS') AS belum_bayar, "
            + "COALESCE(SUM(b.jumlah) FILTER (WHERE " + PAYMENT_STATUS_CASE + " <> 'LUNAS'), 0) AS total_belum_bayar, "
            + "COUNT(*) FILTER (WHERE " + PAYMENT_STATUS_CASE + " = 'TERLAMBAT') AS terlambat, "
            + "COUNT(*) FILTER (WHERE " + PAYMENT_STATUS_CASE + " = 'LUNAS') AS lunas "
            + "FROM pembayaran b WHERE b.penghuni_id = ?";

    public Optional<ResidentProfile> findProfileByUserId(int userId) throws SQLException {
        String sql = """
                SELECT p.id, p.nama_lengkap, p.no_hp, p.pekerjaan_instansi, p.kontak_darurat,
                       p.tanggal_masuk, p.status,
                       k.nomor_kamar, k.lantai, tk.nama AS tipe_nama, tk.harga_bulanan
                FROM penghuni p
                LEFT JOIN kamar k ON k.id = p.kamar_id
                LEFT JOIN tipe_kamar tk ON tk.id = k.tipe_kamar_id
                WHERE p.user_id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapProfile(result)) : Optional.empty();
            }
        }
    }

    public List<Pembayaran> findPayments(int penghuniId) throws SQLException {
        String sql = """
                SELECT b.id, b.penghuni_id, b.periode, b.jumlah, b.jatuh_tempo,
                       b.tanggal_bayar, b.metode,
                """ + PAYMENT_STATUS_CASE + " AS status, b.bukti_url "
                + "FROM pembayaran b WHERE b.penghuni_id = ? "
                + "ORDER BY b.periode DESC, b.id DESC";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, penghuniId);
            try (ResultSet result = statement.executeQuery()) {
                List<Pembayaran> payments = new ArrayList<>();
                while (result.next()) {
                    payments.add(mapPayment(result));
                }
                return payments;
            }
        }
    }

    public List<Keluhan> findComplaints(int penghuniId) throws SQLException {
        String sql = """
                SELECT k.id, k.penghuni_id, k.judul, k.deskripsi, k.kategori, k.prioritas,
                       k.status, k.tanggapan, k.created_at, k.resolved_at, u.nama AS penangan_nama
                FROM keluhan k
                LEFT JOIN users u ON u.id = k.ditangani_oleh
                WHERE k.penghuni_id = ?
                ORDER BY k.created_at DESC, k.id DESC
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, penghuniId);
            try (ResultSet result = statement.executeQuery()) {
                List<Keluhan> complaints = new ArrayList<>();
                while (result.next()) {
                    complaints.add(mapComplaint(result));
                }
                return complaints;
            }
        }
    }

    public ResidentSummary loadSummary(int penghuniId) throws SQLException {
        long belumBayar = 0;
        BigDecimal totalBelumBayar = BigDecimal.ZERO;
        long terlambat = 0;
        long lunas = 0;
        long keluhanTerbuka = 0;
        long keluhanTotal = 0;

        try (Connection connection = Database.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(SUMMARY_SQL)) {
                statement.setInt(1, penghuniId);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        belumBayar = result.getLong("belum_bayar");
                        totalBelumBayar = result.getBigDecimal("total_belum_bayar");
                        terlambat = result.getLong("terlambat");
                        lunas = result.getLong("lunas");
                    }
                }
            }

            String complaintSql = """
                    SELECT COUNT(*) FILTER (WHERE status <> 'SELESAI') AS terbuka,
                           COUNT(*) AS total
                    FROM keluhan
                    WHERE penghuni_id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(complaintSql)) {
                statement.setInt(1, penghuniId);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        keluhanTerbuka = result.getLong("terbuka");
                        keluhanTotal = result.getLong("total");
                    }
                }
            }
        }

        return new ResidentSummary(
                belumBayar,
                totalBelumBayar == null ? BigDecimal.ZERO : totalBelumBayar,
                terlambat,
                lunas,
                keluhanTerbuka,
                keluhanTotal
        );
    }

    public int createComplaint(int penghuniId, Keluhan keluhan) throws SQLException {
        String sql = """
                INSERT INTO keluhan (penghuni_id, judul, deskripsi, kategori, prioritas, status)
                VALUES (?, ?, ?, ?, ?, 'BARU')
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, penghuniId);
            statement.setString(2, keluhan.getJudul());
            statement.setString(3, keluhan.getDeskripsi());
            statement.setString(4, keluhan.getKategori());
            statement.setString(5, keluhan.getPrioritas());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Database tidak mengembalikan id keluhan.");
                }
                return generatedKeys.getInt(1);
            }
        }
    }

    public boolean submitPaymentProof(int penghuniId, int pembayaranId, String buktiUrl)
            throws SQLException {
        String sql = """
                UPDATE pembayaran
                SET bukti_url = ?
                WHERE id = ? AND penghuni_id = ? AND status <> 'LUNAS'
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, buktiUrl);
            statement.setInt(2, pembayaranId);
            statement.setInt(3, penghuniId);
            return statement.executeUpdate() == 1;
        }
    }

    private static ResidentProfile mapProfile(ResultSet result) throws SQLException {
        int lantaiValue = result.getInt("lantai");
        Integer lantai = result.wasNull() ? null : lantaiValue;
        Date tanggalMasuk = result.getDate("tanggal_masuk");
        return new ResidentProfile(
                result.getInt("id"),
                result.getString("nama_lengkap"),
                result.getString("no_hp"),
                result.getString("pekerjaan_instansi"),
                result.getString("kontak_darurat"),
                tanggalMasuk == null ? null : tanggalMasuk.toLocalDate(),
                result.getString("status"),
                result.getString("nomor_kamar"),
                lantai,
                result.getString("tipe_nama"),
                result.getBigDecimal("harga_bulanan")
        );
    }

    private static Pembayaran mapPayment(ResultSet result) throws SQLException {
        Date periode = result.getDate("periode");
        Date jatuhTempo = result.getDate("jatuh_tempo");
        Date tanggalBayar = result.getDate("tanggal_bayar");
        return new Pembayaran(
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
    }

    private static Keluhan mapComplaint(ResultSet result) throws SQLException {
        Timestamp createdAt = result.getTimestamp("created_at");
        Timestamp resolvedAt = result.getTimestamp("resolved_at");
        Keluhan keluhan = new Keluhan(
                result.getInt("id"),
                result.getInt("penghuni_id"),
                null,
                result.getString("judul"),
                result.getString("deskripsi"),
                result.getString("kategori"),
                result.getString("prioritas"),
                result.getString("status"),
                result.getString("tanggapan"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                resolvedAt == null ? null : resolvedAt.toLocalDateTime()
        );
        keluhan.setPenanganNama(result.getString("penangan_nama"));
        return keluhan;
    }

    public record ResidentProfile(
            int penghuniId,
            String namaLengkap,
            String noHp,
            String pekerjaanInstansi,
            String kontakDarurat,
            LocalDate tanggalMasuk,
            String status,
            String nomorKamar,
            Integer lantai,
            String tipeKamarNama,
            BigDecimal hargaBulanan
    ) {
        public int getPenghuniId() {
            return penghuniId;
        }

        public String getNamaLengkap() {
            return namaLengkap;
        }

        public String getNoHp() {
            return noHp;
        }

        public String getPekerjaanInstansi() {
            return pekerjaanInstansi;
        }

        public String getKontakDarurat() {
            return kontakDarurat;
        }

        public LocalDate getTanggalMasuk() {
            return tanggalMasuk;
        }

        public String getStatus() {
            return status;
        }

        public String getNomorKamar() {
            return nomorKamar;
        }

        public Integer getLantai() {
            return lantai;
        }

        public String getTipeKamarNama() {
            return tipeKamarNama;
        }

        public BigDecimal getHargaBulanan() {
            return hargaBulanan;
        }
    }

    public record ResidentSummary(
            long belumBayarCount,
            BigDecimal belumBayarTotal,
            long terlambatCount,
            long lunasCount,
            long keluhanTerbukaCount,
            long keluhanTotalCount
    ) {
        public long getBelumBayarCount() {
            return belumBayarCount;
        }

        public BigDecimal getBelumBayarTotal() {
            return belumBayarTotal;
        }

        public long getTerlambatCount() {
            return terlambatCount;
        }

        public long getLunasCount() {
            return lunasCount;
        }

        public long getKeluhanTerbukaCount() {
            return keluhanTerbukaCount;
        }

        public long getKeluhanTotalCount() {
            return keluhanTotalCount;
        }
    }
}
