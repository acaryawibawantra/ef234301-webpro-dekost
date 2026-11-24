package id.dekost.servlet;

import id.dekost.dao.KeluhanDao;
import id.dekost.model.Keluhan;
import id.dekost.model.User;
import id.dekost.util.Flash;
import id.dekost.util.Pagination;
import id.dekost.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@WebServlet(name = "KeluhanServlet", urlPatterns = "/admin/keluhan/*")
public class KeluhanServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/admin/keluhan/list.jsp";
    private static final String LIST_PATH = "/admin/keluhan";
    private static final Set<String> COMPLAINT_STATUSES = Set.of("BARU", "DIPROSES", "SELESAI");
    private static final List<String> CATEGORIES =
            List.of("FASILITAS", "KEBERSIHAN", "KEBISINGAN", "LAINNYA");
    private static final List<String> PRIORITIES = List.of("RENDAH", "SEDANG", "TINGGI");

    private final KeluhanDao keluhanDao = new KeluhanDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.isBlank() && !"/".equals(pathInfo)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String status = request.getParameter("status");
        Map<String, String> errors = new LinkedHashMap<>();
        if (status != null && !status.isBlank() && !COMPLAINT_STATUSES.contains(status)) {
            errors.put("status", "Filter status tidak valid.");
            status = "";
        }

        Keluhan form = new Keluhan();
        String editId = request.getParameter("edit");
        if (editId != null && !editId.isBlank()) {
            Integer id = parsePositiveInteger(editId);
            if (id == null) {
                errors.put("general", "ID keluhan tidak valid.");
            } else {
                try {
                    Optional<Keluhan> existing = keluhanDao.findById(id);
                    if (existing.isPresent()) {
                        form = existing.get();
                    } else {
                        errors.put("general", "Keluhan tidak ditemukan.");
                    }
                } catch (SQLException e) {
                    throw new ServletException("Gagal memuat keluhan.", e);
                }
            }
        }

        try {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), status, form, errors);
        } catch (SQLException e) {
            throw new ServletException("Gagal memuat daftar keluhan.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if ("/create".equals(pathInfo)) {
                create(request, response);
            } else if ("/update".equals(pathInfo)) {
                update(request, response);
            } else if ("/delete".equals(pathInfo)) {
                delete(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Gagal memproses data keluhan.", e);
        }
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        FormValidation validation = validateForm(request, null);
        if (!validation.errors().isEmpty()) {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        try {
            keluhanDao.create(validation.form());
        } catch (SQLException e) {
            if (!"23503".equals(e.getSQLState())) {
                throw e;
            }
            validation.errors().put("penghuniId", "Penghuni tidak ditemukan.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        Flash.success(request, "Keluhan berhasil ditambahkan.");
        redirectToList(request, response);
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        if (id == null) {
            Map<String, String> errors = Map.of("general", "ID keluhan tidak valid.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validateForm(request, null).form(), errors);
            return;
        }

        Integer adminId = getAdminId(request);
        if (adminId == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Sesi admin tidak valid. Silakan login kembali.");
            return;
        }

        FormValidation validation = validateForm(request, id);
        if (!validation.errors().isEmpty()) {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        KeluhanDao.UpdateResult result;
        try {
            result = keluhanDao.update(validation.form(), adminId);
        } catch (SQLException e) {
            if (!"23503".equals(e.getSQLState())) {
                throw e;
            }
            validation.errors().put("penghuniId", "Penghuni tidak ditemukan.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        switch (result) {
            case UPDATED -> {
                Flash.success(request, "Keluhan berhasil diperbarui.");
                redirectToList(request, response);
            }
            case NOT_FOUND -> {
                Flash.error(request, "Keluhan tidak ditemukan.");
                redirectToList(request, response);
            }
            case CANNOT_REOPEN_TO_NEW -> {
                validation.errors().put("status", "Keluhan selesai tidak dapat dikembalikan ke status BARU.");
                renderList(request, response, request.getParameter("q"),
                        request.getParameter("page"), request.getParameter("status"),
                        validation.form(), validation.errors());
            }
            case INVALID_STATUS -> {
                validation.errors().put("status", "Status keluhan tidak valid.");
                renderList(request, response, request.getParameter("q"),
                        request.getParameter("page"), request.getParameter("status"),
                        validation.form(), validation.errors());
            }
            case INVALID_HANDLER -> {
                Flash.error(request, "Admin pemroses tidak valid. Silakan login kembali.");
                redirectToList(request, response);
            }
        }
    }

    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        if (id == null) {
            Flash.error(request, "ID keluhan tidak valid.");
        } else if (keluhanDao.deleteById(id)) {
            Flash.success(request, "Keluhan berhasil dihapus.");
        } else {
            Flash.error(request, "Keluhan tidak ditemukan.");
        }
        redirectToList(request, response);
    }

    private FormValidation validateForm(HttpServletRequest request, Integer id) {
        Map<String, String> errors = new LinkedHashMap<>();

        Integer penghuniId = parsePositiveInteger(request.getParameter("penghuniId"));
        if (penghuniId == null) {
            errors.put("penghuniId", "Penghuni wajib dipilih.");
        }

        String judul = request.getParameter("judul");
        judul = judul == null ? "" : judul.trim();
        if (judul.isEmpty()) {
            errors.put("judul", "Judul keluhan wajib diisi.");
        } else if (judul.length() > 150) {
            errors.put("judul", "Judul keluhan maksimal 150 karakter.");
        }

        String deskripsi = request.getParameter("deskripsi");
        deskripsi = deskripsi == null ? "" : deskripsi.trim();
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

        String status = id == null ? "BARU" : request.getParameter("status");
        if (status == null || !COMPLAINT_STATUSES.contains(status)) {
            errors.put("status", "Status keluhan tidak valid.");
            status = "BARU";
        }

        String tanggapan = optionalText(request.getParameter("tanggapan"));
        Keluhan form = new Keluhan(
                id, penghuniId, null, judul, deskripsi, kategori, prioritas, status,
                tanggapan, null, null);
        return new FormValidation(form, errors);
    }

    private void renderList(HttpServletRequest request, HttpServletResponse response,
                            String query, String requestedPage, String status, Keluhan form,
                            Map<String, String> errors)
            throws SQLException, ServletException, IOException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedStatus = status == null ? "" : status.trim();
        if (!normalizedStatus.isEmpty() && !COMPLAINT_STATUSES.contains(normalizedStatus)) {
            normalizedStatus = "";
        }

        long totalItems = keluhanDao.count(normalizedQuery, normalizedStatus);
        Pagination pagination = Pagination.of(requestedPage, totalItems);
        request.setAttribute("keluhanList", keluhanDao.findPage(
                normalizedQuery, normalizedStatus, pagination.getPageSize(), pagination.getOffset()));
        request.setAttribute("penghuniOptions", keluhanDao.findOccupantOptions());
        request.setAttribute("pagination", pagination);
        request.setAttribute("q", normalizedQuery);
        request.setAttribute("status", normalizedStatus);
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        request.setAttribute("categories", CATEGORIES);
        request.setAttribute("priorities", PRIORITIES);
        request.setAttribute("statuses", List.of("BARU", "DIPROSES", "SELESAI"));
        request.setAttribute("pageTitle", "Keluhan");
        request.setAttribute("activeNav", "keluhan");
        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static Integer getAdminId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionUser = session == null
                ? null
                : session.getAttribute(AuthServlet.USER_SESSION_ATTRIBUTE);
        if (!(sessionUser instanceof User user)
                || !"ADMIN".equals(user.getRole())
                || user.getId() == null
                || user.getId() < 1) {
            return null;
        }
        return user.getId();
    }

    private static Integer parsePositiveInteger(String value) {
        if (!Validator.isPositiveInteger(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String optionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void redirectToList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + LIST_PATH);
    }

    private record FormValidation(Keluhan form, Map<String, String> errors) {
    }
}
