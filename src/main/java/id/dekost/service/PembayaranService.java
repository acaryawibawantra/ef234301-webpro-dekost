package id.dekost.service;

import id.dekost.dao.PembayaranDao;
import id.dekost.model.Pembayaran;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

public class PembayaranService {

    private static final Set<String> PAYMENT_METHODS = Set.of("TUNAI", "TRANSFER", "QRIS");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999.99");

    private final PembayaranDao pembayaranDao;

    public PembayaranService() {
        this(new PembayaranDao());
    }

    public PembayaranService(PembayaranDao pembayaranDao) {
        this.pembayaranDao = Objects.requireNonNull(pembayaranDao, "pembayaranDao");
    }

    public MutationResult create(Pembayaran pembayaran) throws SQLException {
        if (!isValidEditablePayment(pembayaran, false)) {
            return MutationResult.INVALID_DATA;
        }
        try {
            pembayaranDao.create(pembayaran);
            return MutationResult.SAVED;
        } catch (SQLException e) {
            return mapWriteError(e);
        }
    }

    public MutationResult update(Pembayaran pembayaran) throws SQLException {
        if (!isValidEditablePayment(pembayaran, true)) {
            return MutationResult.INVALID_DATA;
        }
        try {
            if (pembayaranDao.update(pembayaran)) {
                return MutationResult.SAVED;
            }
        } catch (SQLException e) {
            return mapWriteError(e);
        }

        return findUneditablePaymentResult(pembayaran.getId());
    }

    public MutationResult markPaid(int id, LocalDate tanggalBayar, String metode) throws SQLException {
        if (id < 1 || tanggalBayar == null || !PAYMENT_METHODS.contains(metode)) {
            return MutationResult.INVALID_PAYMENT_DETAILS;
        }
        try {
            if (pembayaranDao.markPaid(id, java.sql.Date.valueOf(tanggalBayar), metode)) {
                return MutationResult.PAID;
            }
        } catch (SQLException e) {
            return mapWriteError(e);
        }

        return findUneditablePaymentResult(id);
    }

    public MutationResult delete(int id) throws SQLException {
        if (id < 1) {
            return MutationResult.NOT_FOUND;
        }
        return pembayaranDao.deleteById(id) ? MutationResult.DELETED : MutationResult.NOT_FOUND;
    }

    public int generateForCurrentMonth() throws SQLException {
        return pembayaranDao.generateForCurrentMonth();
    }

    private MutationResult findUneditablePaymentResult(int id) throws SQLException {
        return pembayaranDao.findById(id)
                .map(payment -> "LUNAS".equals(payment.getStatus())
                        ? MutationResult.ALREADY_PAID
                        : MutationResult.CONFLICT)
                .orElse(MutationResult.NOT_FOUND);
    }

    private static MutationResult mapWriteError(SQLException error) throws SQLException {
        if ("23505".equals(error.getSQLState())) {
            return MutationResult.DUPLICATE_PERIOD;
        }
        if ("23503".equals(error.getSQLState())) {
            return MutationResult.OCCUPANT_NOT_FOUND;
        }
        throw error;
    }

    private static boolean isValidEditablePayment(Pembayaran pembayaran, boolean requireId) {
        if (pembayaran == null
                || pembayaran.getPenghuniId() == null
                || pembayaran.getPenghuniId() < 1
                || pembayaran.getPeriode() == null
                || pembayaran.getPeriode().getDayOfMonth() != 1
                || pembayaran.getJumlah() == null
                || pembayaran.getJumlah().signum() < 0
                || pembayaran.getJumlah().scale() > 2
                || pembayaran.getJumlah().compareTo(MAX_AMOUNT) > 0
                || pembayaran.getJatuhTempo() == null
                || pembayaran.getJatuhTempo().isBefore(pembayaran.getPeriode())
                || (requireId && (pembayaran.getId() == null || pembayaran.getId() < 1))) {
            return false;
        }
        return true;
    }

    public enum MutationResult {
        SAVED,
        PAID,
        DELETED,
        DUPLICATE_PERIOD,
        OCCUPANT_NOT_FOUND,
        NOT_FOUND,
        ALREADY_PAID,
        INVALID_PAYMENT_DETAILS,
        INVALID_DATA,
        CONFLICT
    }
}
