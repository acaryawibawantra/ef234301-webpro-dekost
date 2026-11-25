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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDao {

    public Map<String, Long> getRoomOccupancy() throws SQLException {
        String sql = "SELECT status, COUNT(*) AS jumlah FROM kamar GROUP BY status";
        Map<String, Long> occupancy = new LinkedHashMap<>();
        occupancy.put("TERSEDIA", 0L);
        occupancy.put("TERISI", 0L);
        occupancy.put("PERBAIKAN", 0L);

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                occupancy.put(result.getString("status"), result.getLong("jumlah"));
            }
        }
        return occupancy;
    }

    public List<FloorOccupancy> getOccupancyByFloor() throws SQLException {
        String sql = """
                SELECT lantai, COUNT(*) AS total_kamar,
                       SUM(CASE WHEN status = 'TERISI' THEN 1 ELSE 0 END) AS kamar_terisi
                FROM kamar
                GROUP BY lantai
                ORDER BY lantai
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<FloorOccupancy> occupancy = new ArrayList<>();
            while (result.next()) {
                occupancy.add(new FloorOccupancy(
                        result.getInt("lantai"),
                        result.getLong("total_kamar"),
                        result.getLong("kamar_terisi")
                ));
            }
            return occupancy;
        }
    }

    public BigDecimal getMonthlyRevenue() throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(jumlah), 0) AS total
                FROM pembayaran
                WHERE status = 'LUNAS'
                  AND periode >= DATE_TRUNC('month', CURRENT_DATE)::date
                  AND periode < (DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month')::date
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getBigDecimal("total");
        }
    }

    public CurrentMonthBillingSummary getCurrentMonthBillingSummary() throws SQLException {
        String sql = """
                SELECT COUNT(*) AS total_tagihan,
                       COALESCE(SUM(CASE WHEN status = 'LUNAS' THEN 1 ELSE 0 END), 0) AS tagihan_lunas
                FROM pembayaran
                WHERE periode >= DATE_TRUNC('month', CURRENT_DATE)::date
                  AND periode < (DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month')::date
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return new CurrentMonthBillingSummary(
                    result.getLong("tagihan_lunas"),
                    result.getLong("total_tagihan")
            );
        }
    }

    public BillingSummary getOverdueBillingSummary() throws SQLException {
        String sql = """
                SELECT COUNT(*) AS jumlah_tagihan,
                       COALESCE(SUM(jumlah), 0) AS total_jumlah
                FROM pembayaran
                WHERE status <> 'LUNAS' AND jatuh_tempo < CURRENT_DATE
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return new BillingSummary(
                    result.getLong("jumlah_tagihan"),
                    result.getBigDecimal("total_jumlah")
            );
        }
    }

    public OpenComplaintSummary getOpenComplaintSummary() throws SQLException {
        String sql = """
                SELECT COUNT(*) AS jumlah_terbuka,
                       COALESCE(SUM(CASE WHEN prioritas = 'TINGGI' THEN 1 ELSE 0 END), 0)
                           AS prioritas_tinggi
                FROM keluhan
                WHERE status <> 'SELESAI'
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return new OpenComplaintSummary(
                    result.getLong("jumlah_terbuka"),
                    result.getLong("prioritas_tinggi")
            );
        }
    }

    public List<Pembayaran> findUpcomingPayments() throws SQLException {
        String sql = """
                SELECT b.id, b.penghuni_id, p.nama_lengkap AS penghuni_nama,
                       p.no_hp AS penghuni_no_hp, k.nomor_kamar,
                       b.periode, b.jumlah, b.jatuh_tempo,
                       b.tanggal_bayar, b.metode,
                       CASE
                           WHEN b.status = 'LUNAS' THEN 'LUNAS'
                           WHEN b.jatuh_tempo < CURRENT_DATE THEN 'TERLAMBAT'
                           ELSE 'BELUM_BAYAR'
                       END AS status,
                       b.bukti_url
                FROM pembayaran b
                JOIN penghuni p ON p.id = b.penghuni_id
                LEFT JOIN kamar k ON k.id = p.kamar_id
                WHERE b.status <> 'LUNAS'
                ORDER BY b.jatuh_tempo ASC, b.id ASC
                LIMIT 5
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Pembayaran> payments = new ArrayList<>();
            while (result.next()) {
                payments.add(mapPayment(result));
            }
            return payments;
        }
    }

    public List<Keluhan> findRecentComplaints() throws SQLException {
        String sql = """
                SELECT k.id, k.penghuni_id, p.nama_lengkap AS penghuni_nama,
                       km.nomor_kamar, k.ditangani_oleh, u.nama AS penangan_nama,
                       k.judul, k.deskripsi, k.kategori, k.prioritas, k.status,
                       k.tanggapan, k.created_at, k.resolved_at
                FROM keluhan k
                JOIN penghuni p ON p.id = k.penghuni_id
                LEFT JOIN kamar km ON km.id = p.kamar_id
                LEFT JOIN users u ON u.id = k.ditangani_oleh
                ORDER BY k.created_at DESC, k.id DESC
                LIMIT 3
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Keluhan> complaints = new ArrayList<>();
            while (result.next()) {
                complaints.add(mapComplaint(result));
            }
            return complaints;
        }
    }

    private static Pembayaran mapPayment(ResultSet result) throws SQLException {
        Date periode = result.getDate("periode");
        Date jatuhTempo = result.getDate("jatuh_tempo");
        Date tanggalBayar = result.getDate("tanggal_bayar");
        Pembayaran payment = new Pembayaran(
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
        payment.setPenghuniNama(result.getString("penghuni_nama"));
        payment.setPenghuniNoHp(result.getString("penghuni_no_hp"));
        payment.setNomorKamar(result.getString("nomor_kamar"));
        return payment;
    }

    private static Keluhan mapComplaint(ResultSet result) throws SQLException {
        int handlerValue = result.getInt("ditangani_oleh");
        Integer handlerId = result.wasNull() ? null : handlerValue;
        Timestamp createdAt = result.getTimestamp("created_at");
        Timestamp resolvedAt = result.getTimestamp("resolved_at");
        Keluhan complaint = new Keluhan(
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
        complaint.setPenghuniNama(result.getString("penghuni_nama"));
        complaint.setNomorKamar(result.getString("nomor_kamar"));
        complaint.setPenanganNama(result.getString("penangan_nama"));
        return complaint;
    }

    public record FloorOccupancy(int lantai, long totalKamar, long kamarTerisi) {
        public int getLantai() {
            return lantai;
        }

        public long getTotalKamar() {
            return totalKamar;
        }

        public long getKamarTerisi() {
            return kamarTerisi;
        }
    }

    public record BillingSummary(long jumlahTagihan, BigDecimal totalJumlah) {
        public long getJumlahTagihan() {
            return jumlahTagihan;
        }

        public BigDecimal getTotalJumlah() {
            return totalJumlah;
        }
    }

    public record OpenComplaintSummary(long jumlahTerbuka, long prioritasTinggi) {
        public long getJumlahTerbuka() {
            return jumlahTerbuka;
        }

        public long getPrioritasTinggi() {
            return prioritasTinggi;
        }
    }

    public record CurrentMonthBillingSummary(long lunasCount, long totalCount) {
        public long getLunasCount() {
            return lunasCount;
        }

        public long getTotalCount() {
            return totalCount;
        }
    }
}
