package id.dekost.servlet;

import id.dekost.dao.PortalDao;
import id.dekost.model.Keluhan;
import id.dekost.model.User;
import id.dekost.util.Flash;
import id.dekost.util.TanggalFormatter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@WebServlet(name = "PenghuniKeluhanServlet", urlPatterns = "/penghuni/keluhan/*")
public class PenghuniKeluhanServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/penghuni/keluhan.jsp";
    private static final String LIST_PATH = "/penghuni/keluhan";
    private static final Set<String> FILTER_STATUSES = Set.of("BARU", "DIPROSES", "SELESAI");
    private static final List<String> CATEGORIES =
            List.of("FASILITAS", "KEBERSIHAN", "KEBISINGAN", "LAINNYA");
    private static final List<String> PRIORITIES = List.of("RENDAH", "SEDANG", "TINGGI");

    private final PortalDao portalDao = new PortalDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = currentUserId(request);
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Sesi penghuni tidak valid. Silakan login kembali.");
            return;
        }

        try {
            Optional<PortalDao.ResidentProfile> profileOptional =
                    portalDao.findProfileByUserId(userId);
            if (profileOptional.isEmpty()) {
                renderMissingProfile(request, response);
                return;
            }

            String status = request.getParameter("status");
            if (status == null || !FILTER_STATUSES.contains(status)) {
                status = "";
            }

            renderList(request, response, profileOptional.get(), status, new Keluhan(), Map.of());
        } catch (SQLException e) {
            throw new ServletException("Gagal memuat keluhan penghuni.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (!"/create".equals(pathInfo)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Integer userId = currentUserId(request);
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Sesi penghuni tidak valid. Silakan login kembali.");
            return;
        }

        try {
            Optional<PortalDao.ResidentProfile> profileOptional =
                    portalDao.findProfileByUserId(userId);
            if (profileOptional.isEmpty()) {
                Flash.error(request, "Akun Anda belum terhubung ke data penghuni.");
                response.sendRedirect(request.getContextPath() + LIST_PATH);
                return;
            }
            create(request, response, profileOptional.get());
        } catch (SQLException e) {
            throw new ServletException("Gagal menyimpan keluhan.", e);
        }
    }

    private void create(HttpServletRequest request, HttpServletResponse response,
                        PortalDao.ResidentProfile profile)
            throws SQLException, ServletException, IOException {
        Map<String, String> errors = new LinkedHashMap<>();

        String judul = trim(request.getParameter("judul"));
        if (judul.isEmpty()) {
            errors.put("judul", "Judul keluhan wajib diisi.");
        } else if (judul.length() > 150) {
            errors.put("judul", "Judul keluhan maksimal 150 karakter.");
        }

        String deskripsi = trim(request.getParameter("deskripsi"));
        if (deskripsi.isEmpty()) {
            errors.put("deskripsi", "Deskripsi keluhan wajib diisi.");
        }

        String kategori = request.getParameter("kategori");
        if (kategori == null || kategori.isBlank()) {
            kategori = "LAINNYA";
        }
        if (!CATEGORIES.contains(kategori)) {
            errors.put("kategori", "Kategori keluhan tidak valid.");
        }

        String prioritas = request.getParameter("prioritas");
        if (prioritas == null || prioritas.isBlank()) {
            prioritas = "SEDANG";
        }
        if (!PRIORITIES.contains(prioritas)) {
            errors.put("prioritas", "Prioritas keluhan tidak valid.");
        }

        String status = request.getParameter("status");
        if (status == null || !FILTER_STATUSES.contains(status)) {
            status = "";
        }

        Keluhan form = new Keluhan(null, profile.getPenghuniId(), null, judul, deskripsi,
                kategori, prioritas, "BARU", null, null, null);

        if (!errors.isEmpty()) {
            renderList(request, response, profile, status, form, errors);
            return;
        }

        portalDao.createComplaint(profile.getPenghuniId(), form);
        Flash.success(request, "Keluhan berhasil dikirim.");
        response.sendRedirect(request.getContextPath() + LIST_PATH);
    }

    private void renderList(HttpServletRequest request, HttpServletResponse response,
                            PortalDao.ResidentProfile profile, String status, Keluhan form,
                            Map<String, String> errors)
            throws SQLException, ServletException, IOException {
        List<Keluhan> complaints = portalDao.findComplaints(profile.getPenghuniId());
        List<Keluhan> visible = new ArrayList<>();
        for (Keluhan complaint : complaints) {
            if (status.isEmpty() || status.equals(complaint.getStatus())) {
                visible.add(complaint);
            }
        }

        Map<Integer, String> complaintDates = new LinkedHashMap<>();
        Map<Integer, String> complaintResolvedDates = new LinkedHashMap<>();
        for (Keluhan complaint : visible) {
            if (complaint.getId() == null) {
                continue;
            }
            LocalDate createdDate = complaint.getCreatedAt() == null
                    ? null
                    : complaint.getCreatedAt().toLocalDate();
            LocalDate resolvedDate = complaint.getResolvedAt() == null
                    ? null
                    : complaint.getResolvedAt().toLocalDate();
            complaintDates.put(complaint.getId(), formatDate(createdDate));
            complaintResolvedDates.put(complaint.getId(), formatDate(resolvedDate));
        }

        request.setAttribute("profile", profile);
        request.setAttribute("keluhanList", visible);
        request.setAttribute("totalKeluhan", complaints.size());
        request.setAttribute("complaintDates", complaintDates);
        request.setAttribute("complaintResolvedDates", complaintResolvedDates);
        request.setAttribute("status", status);
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        request.setAttribute("categories", CATEGORIES);
        request.setAttribute("priorities", PRIORITIES);
        request.setAttribute("pageTitle", "Keluhan Saya");
        request.setAttribute("pageSubtitle", complaints.size() + " keluhan");
        request.setAttribute("pageActionLabel", "Buat keluhan");
        request.setAttribute("pageActionModal", "keluhan-create-modal");
        request.setAttribute("activeNav", "keluhan");
        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private void renderMissingProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("profileMissing", Boolean.TRUE);
        request.setAttribute("pageTitle", "Keluhan Saya");
        request.setAttribute("pageSubtitle", "Akun Anda belum terhubung ke data penghuni");
        request.setAttribute("activeNav", "keluhan");
        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "-" : TanggalFormatter.format(date);
    }

    private static Integer currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionUser = session == null
                ? null
                : session.getAttribute(AuthServlet.USER_SESSION_ATTRIBUTE);
        if (sessionUser instanceof User user && user.getId() != null) {
            return user.getId();
        }
        return null;
    }
}
