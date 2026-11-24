package id.dekost.service;

import id.dekost.config.Database;
import id.dekost.dao.PenghuniDao;
import id.dekost.model.Penghuni;
import id.dekost.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class PenghuniService {

    private final PenghuniDao penghuniDao;

    public PenghuniService() {
        this(new PenghuniDao());
    }

    public PenghuniService(PenghuniDao penghuniDao) {
        this.penghuniDao = Objects.requireNonNull(penghuniDao, "penghuniDao");
    }

    public CheckInResult checkIn(Penghuni penghuni) throws SQLException {
        if (!isValidOccupant(penghuni)
                || penghuni.getKamarId() == null
                || penghuni.getKamarId() < 1) {
            return CheckInResult.INVALID_DATA;
        }

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int roomId = penghuni.getKamarId();
                Optional<String> roomStatus = penghuniDao.lockRoom(connection, roomId);
                if (roomStatus.isEmpty()) {
                    connection.rollback();
                    return CheckInResult.ROOM_NOT_FOUND;
                }
                if (!"TERSEDIA".equals(roomStatus.get())
                        || penghuniDao.hasAssignedOccupant(connection, roomId)) {
                    connection.rollback();
                    return CheckInResult.ROOM_UNAVAILABLE;
                }

                penghuniDao.insertActive(connection, penghuni);
                if (!penghuniDao.setRoomStatus(
                        connection, roomId, "TERSEDIA", "TERISI")) {
                    connection.rollback();
                    return CheckInResult.ROOM_UNAVAILABLE;
                }

                connection.commit();
                return CheckInResult.CHECKED_IN;
            } catch (SQLException e) {
                rollback(connection, e);
                throw e;
            }
        }
    }

    public CheckOutResult checkOut(int penghuniId, LocalDate tanggalKeluar) throws SQLException {
        if (tanggalKeluar == null) {
            return CheckOutResult.INVALID_DATE;
        }

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Optional<PenghuniDao.Assignment> initial =
                        penghuniDao.findAssignment(connection, penghuniId);
                if (initial.isEmpty()) {
                    connection.rollback();
                    return CheckOutResult.OCCUPANT_NOT_FOUND;
                }
                PenghuniDao.Assignment initialAssignment = initial.get();
                if (!"AKTIF".equals(initialAssignment.status())) {
                    connection.rollback();
                    return CheckOutResult.NOT_ACTIVE;
                }
                if (initialAssignment.kamarId() == null) {
                    connection.rollback();
                    return CheckOutResult.NO_ACTIVE_ROOM;
                }
                if (initialAssignment.tanggalMasuk() != null
                        && tanggalKeluar.isBefore(initialAssignment.tanggalMasuk())) {
                    connection.rollback();
                    return CheckOutResult.INVALID_DATE;
                }

                int roomId = initialAssignment.kamarId();
                Optional<String> roomStatus = penghuniDao.lockRoom(connection, roomId);
                if (roomStatus.isEmpty()) {
                    connection.rollback();
                    return CheckOutResult.ASSIGNMENT_CHANGED;
                }

                Optional<PenghuniDao.Assignment> locked =
                        penghuniDao.lockAssignment(connection, penghuniId);
                if (locked.isEmpty() || !"AKTIF".equals(locked.get().status())) {
                    connection.rollback();
                    return CheckOutResult.NOT_ACTIVE;
                }
                if (!Integer.valueOf(roomId).equals(locked.get().kamarId())) {
                    connection.rollback();
                    return CheckOutResult.ASSIGNMENT_CHANGED;
                }

                if (!penghuniDao.checkOut(connection, penghuniId, roomId, tanggalKeluar)
                        || !penghuniDao.setRoomStatus(
                        connection, roomId, roomStatus.get(), "TERSEDIA")) {
                    connection.rollback();
                    return CheckOutResult.ASSIGNMENT_CHANGED;
                }

                connection.commit();
                return CheckOutResult.CHECKED_OUT;
            } catch (SQLException e) {
                rollback(connection, e);
                throw e;
            }
        }
    }

    public MoveResult moveRoom(int penghuniId, int newRoomId) throws SQLException {
        return updateAndMove(null, penghuniId, newRoomId);
    }

    public MoveResult updateAndMove(Penghuni updatedPenghuni, int penghuniId, int newRoomId)
            throws SQLException {
        if (penghuniId < 1 || newRoomId < 1
                || (updatedPenghuni != null
                    && (!isValidOccupant(updatedPenghuni)
                        || !Objects.equals(updatedPenghuni.getId(), penghuniId)
                        || !Objects.equals(updatedPenghuni.getKamarId(), newRoomId)))) {
            return MoveResult.INVALID_DATA;
        }

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Optional<PenghuniDao.Assignment> initial =
                        penghuniDao.findAssignment(connection, penghuniId);
                if (initial.isEmpty()) {
                    connection.rollback();
                    return MoveResult.OCCUPANT_NOT_FOUND;
                }
                PenghuniDao.Assignment initialAssignment = initial.get();
                if (!"AKTIF".equals(initialAssignment.status())) {
                    connection.rollback();
                    return MoveResult.NOT_ACTIVE;
                }
                if (initialAssignment.kamarId() == null) {
                    connection.rollback();
                    return MoveResult.NO_ACTIVE_ROOM;
                }

                int oldRoomId = initialAssignment.kamarId();
                if (oldRoomId == newRoomId) {
                    Optional<String> currentRoomStatus =
                            penghuniDao.lockRoom(connection, oldRoomId);
                    Optional<PenghuniDao.Assignment> locked =
                            penghuniDao.lockAssignment(connection, penghuniId);
                    if (currentRoomStatus.isEmpty()
                            || locked.isEmpty()
                            || !"AKTIF".equals(locked.get().status())
                            || !Integer.valueOf(oldRoomId).equals(locked.get().kamarId())) {
                        connection.rollback();
                        return MoveResult.ASSIGNMENT_CHANGED;
                    }
                    if (updatedPenghuni == null) {
                        connection.rollback();
                        return MoveResult.SAME_ROOM;
                    }
                    if (!penghuniDao.updateActiveProfile(connection, updatedPenghuni)) {
                        connection.rollback();
                        return MoveResult.ASSIGNMENT_CHANGED;
                    }
                    connection.commit();
                    return MoveResult.UPDATED;
                }

                int firstRoomId = Math.min(oldRoomId, newRoomId);
                int secondRoomId = Math.max(oldRoomId, newRoomId);
                Optional<String> firstStatus = penghuniDao.lockRoom(connection, firstRoomId);
                Optional<String> secondStatus = penghuniDao.lockRoom(connection, secondRoomId);
                if (firstStatus.isEmpty() || secondStatus.isEmpty()) {
                    connection.rollback();
                    return MoveResult.ROOM_NOT_FOUND;
                }
                String oldRoomStatus = oldRoomId == firstRoomId
                        ? firstStatus.get()
                        : secondStatus.get();
                String newRoomStatus = newRoomId == firstRoomId
                        ? firstStatus.get()
                        : secondStatus.get();

                Optional<PenghuniDao.Assignment> locked =
                        penghuniDao.lockAssignment(connection, penghuniId);
                if (locked.isEmpty() || !"AKTIF".equals(locked.get().status())) {
                    connection.rollback();
                    return MoveResult.NOT_ACTIVE;
                }
                if (!Integer.valueOf(oldRoomId).equals(locked.get().kamarId())) {
                    connection.rollback();
                    return MoveResult.ASSIGNMENT_CHANGED;
                }
                if (!"TERSEDIA".equals(newRoomStatus)
                        || penghuniDao.hasAssignedOccupant(connection, newRoomId)) {
                    connection.rollback();
                    return MoveResult.ROOM_UNAVAILABLE;
                }

                if (!penghuniDao.updateRoomAssignment(
                        connection, penghuniId, oldRoomId, newRoomId)
                        || (updatedPenghuni != null
                            && !penghuniDao.updateActiveProfile(connection, updatedPenghuni))
                        || !penghuniDao.setRoomStatus(
                        connection, oldRoomId, oldRoomStatus, "TERSEDIA")
                        || !penghuniDao.setRoomStatus(
                        connection, newRoomId, "TERSEDIA", "TERISI")) {
                    connection.rollback();
                    return MoveResult.ASSIGNMENT_CHANGED;
                }

                connection.commit();
                return MoveResult.MOVED;
            } catch (SQLException e) {
                rollback(connection, e);
                throw e;
            }
        }
    }

    private static boolean isValidOccupant(Penghuni penghuni) {
        return penghuni != null
                && penghuni.getNamaLengkap() != null
                && !penghuni.getNamaLengkap().isBlank()
                && penghuni.getNamaLengkap().length() <= 100
                && Validator.isValidPhoneNumber(penghuni.getNoHp())
                && (penghuni.getPekerjaanInstansi() == null
                    || penghuni.getPekerjaanInstansi().length() <= 100)
                && (penghuni.getKontakDarurat() == null
                    || penghuni.getKontakDarurat().length() <= 100)
                && penghuni.getTanggalMasuk() != null;
    }

    private static void rollback(Connection connection, SQLException originalError) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            originalError.addSuppressed(rollbackError);
        }
    }

    public enum CheckInResult {
        CHECKED_IN,
        INVALID_DATA,
        ROOM_NOT_FOUND,
        ROOM_UNAVAILABLE
    }

    public enum CheckOutResult {
        CHECKED_OUT,
        INVALID_DATE,
        OCCUPANT_NOT_FOUND,
        NOT_ACTIVE,
        NO_ACTIVE_ROOM,
        ASSIGNMENT_CHANGED
    }

    public enum MoveResult {
        MOVED,
        UPDATED,
        INVALID_DATA,
        OCCUPANT_NOT_FOUND,
        NOT_ACTIVE,
        NO_ACTIVE_ROOM,
        SAME_ROOM,
        ROOM_NOT_FOUND,
        ROOM_UNAVAILABLE,
        ASSIGNMENT_CHANGED
    }
}
