package id.dekost.servlet;

import at.favre.lib.crypto.bcrypt.BCrypt;
import id.dekost.dao.PenghuniDao;
import id.dekost.dao.UserDao;
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
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@WebServlet(name = "AkunServlet", urlPatterns = "/admin/akun/*")
public class AkunServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/admin/akun/list.jsp";
    private static final String LIST_PATH = "/admin/akun";
    private static final Set<String> ROLES = Set.of("ADMIN", "PENGHUNI");
    private static final int BCRYPT_COST = 12;
    private static final DateTimeFormatter CREATED_FORMAT =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.forLanguageTag("id-ID"));

    private final UserDao userDao = new UserDao();
    private final PenghuniDao penghuniDao = new PenghuniDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.isBlank() && !"/".equals(pathInfo)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String role = request.getParameter("role");
        Map<String, String> errors = new LinkedHashMap<>();
        if (role != null && !role.isBlank() && !ROLES.contains(role)) {
            errors.put("role", "Filter role tidak valid.");
            role = "";
        }

        User form = new User();
        Integer selectedPenghuniId = null;
        String editId = request.getParameter("edit");
        if (editId != null && !editId.isBlank()) {
            Integer id = parsePositiveInteger(editId);
            if (id == null) {
                errors.put("general", "ID akun tidak valid.");
            } else {
                try {
                    Optional<UserDao.AccountRow> existing = userDao.findById(id);
                    if (existing.isPresent()) {
                        UserDao.AccountRow account = existing.get();
                        form = new User(account.getId(), account.getNama(), account.getEmail(),
                                null, account.getRole(), account.getCreatedAt());
                        selectedPenghuniId = account.getPenghuniId();
                        request.setAttribute("linkedPenghuniNama", account.getPenghuniNama());
                        request.setAttribute("linkedPenghuniKamar", account.getNomorKamar());
                    } else {
                        errors.put("general", "Akun tidak ditemukan.");
                    }
                } catch (SQLException e) {
                    throw new ServletException("Gagal memuat akun.", e);
                }
            }
        }

        try {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), role, form, errors, selectedPenghuniId);
        } catch (SQLException e) {
            throw new ServletException("Gagal memuat daftar akun.", e);
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
            throw new ServletException("Gagal memproses data akun.", e);
        }
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        FormValidation validation = validateForm(request, null);
        if (!validation.errors().isEmpty()) {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("returnRole"),
                    validation.form(), validation.errors(), validation.selectedPenghuniId());
            return;
        }

        if (userDao.existsByEmail(validation.form().getEmail(), null)) {
            validation.errors().put("email", "Email sudah digunakan akun lain.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("returnRole"),
                    validation.form(), validation.errors(), validation.selectedPenghuniId());
            return;
        }

        int userId;
        try {
            userId = userDao.create(validation.form(), hashPassword(validation.password()));
        } catch (SQLException e) {
            if (!"23505".equals(e.getSQLState())) {
                throw e;
            }
            validation.errors().put("email", "Email sudah digunakan akun lain.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("returnRole"),
                    validation.form(), validation.errors(), validation.selectedPenghuniId());
            return;
        }

        if (validation.selectedPenghuniId() != null
                && !userDao.linkPenghuni(userId, validation.selectedPenghuniId())) {
            userDao.deleteById(userId);
            validation.errors().put("penghuniId",
                    "Penghuni sudah memiliki akun. Pilih penghuni lain atau kosongkan.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("returnRole"),
                    validation.form(), validation.errors(), validation.selectedPenghuniId());
            return;
        }

        Flash.success(request, "Akun berhasil ditambahkan.");
        redirectToList(request, response);
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        if (id == null) {
            Flash.error(request, "ID akun tidak valid.");
            redirectToList(request, response);
            return;
        }

        Optional<UserDao.AccountRow> existing = userDao.findById(id);
        if (existing.isEmpty()) {
            Flash.error(request, "Akun tidak ditemukan.");
            redirectToList(request, response);
            return;
        }

        FormValidation validation = validateForm(request, id);
        Integer currentAdminId = currentUserId(request);
        if (!validation.errors().isEmpty()) {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("returnRole"),
                    validation.form(), validation.errors(), existing.get().getPenghuniId());
            return;
        }

        if (userDao.existsByEmail(validation.form().getEmail(), id)) {
            validation.errors().put("email", "Email sudah digunakan akun lain.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("returnRole"),
                    validation.form(), validation.errors(), existing.get().getPenghuniId());
            return;
        }

        if (id.equals(currentAdminId) && !"ADMIN".equals(validation.form().getRole())) {
            validation.errors().put("role", "Anda tidak dapat menurunkan role akun sendiri.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("returnRole"),
                    validation.form(), validation.errors(), existing.get().getPenghuniId());
            return;
        }

        try {
            if (!userDao.update(validation.form())) {
                Flash.error(request, "Akun tidak ditemukan.");
                redirectToList(request, response);
                return;
            }
        } catch (SQLException e) {
            if (!"23505".equals(e.getSQLState())) {
                throw e;
            }
            validation.errors().put("email", "Email sudah digunakan akun lain.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("returnRole"),
                    validation.form(), validation.errors(), existing.get().getPenghuniId());
            return;
        }

        if (validation.password() != null) {
            userDao.updatePassword(id, hashPassword(validation.password()));
        }

        Flash.success(request, "Akun berhasil diperbarui.");
        redirectToList(request, response);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        if (id == null) {
            Flash.error(request, "ID akun tidak valid.");
            redirectToList(request, response);
            return;
        }

        if (id.equals(currentUserId(request))) {
            Flash.error(request, "Anda tidak dapat menghapus akun yang sedang digunakan.");
            redirectToList(request, response);
            return;
        }

        Optional<UserDao.AccountRow> existing = userDao.findById(id);
        if (existing.isEmpty()) {
            Flash.error(request, "Akun tidak ditemukan.");
            redirectToList(request, response);
            return;
        }

        if ("ADMIN".equals(existing.get().getRole()) && userDao.countByRole("ADMIN") <= 1) {
            Flash.error(request, "Akun admin terakhir tidak dapat dihapus.");
            redirectToList(request, response);
            return;
        }

        if (userDao.deleteById(id)) {
            Flash.success(request, "Akun berhasil dihapus.");
        } else {
            Flash.error(request, "Akun tidak ditemukan.");
        }
        redirectToList(request, response);
    }

    private FormValidation validateForm(HttpServletRequest request, Integer id) {
        Map<String, String> errors = new LinkedHashMap<>();

        String nama = request.getParameter("nama");
        nama = nama == null ? "" : nama.trim();
        if (nama.isEmpty()) {
            errors.put("nama", "Nama akun wajib diisi.");
        } else if (nama.length() > 100) {
            errors.put("nama", "Nama akun maksimal 100 karakter.");
        }

        String email = request.getParameter("email");
        email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (email.isEmpty()) {
            errors.put("email", "Email wajib diisi.");
        } else if (email.length() > 120) {
            errors.put("email", "Email maksimal 120 karakter.");
        } else if (!Validator.isValidEmail(email)) {
            errors.put("email", "Format email tidak valid.");
        }

        String role = request.getParameter("role");
        if (role == null || !ROLES.contains(role)) {
            errors.put("role", "Role akun tidak valid.");
        }

        String password = request.getParameter("password");
        password = password == null || password.isEmpty() ? null : password;
        if (id == null) {
            if (password == null) {
                errors.put("password", "Password wajib diisi.");
            } else if (!isValidPassword(password)) {
                errors.put("password", "Password harus 8 sampai 72 karakter.");
            }
        } else if (password != null && !isValidPassword(password)) {
            errors.put("password", "Password baru harus 8 sampai 72 karakter.");
        }

        Integer selectedPenghuniId = parsePositiveInteger(request.getParameter("penghuniId"));
        if (selectedPenghuniId != null && !"PENGHUNI".equals(role)) {
            errors.put("penghuniId", "Hanya akun penghuni yang dapat ditautkan ke data penghuni.");
        }

        User form = new User(id, nama, email, null, role, null);
        return new FormValidation(form, errors, password, selectedPenghuniId);
    }

    private void renderList(HttpServletRequest request, HttpServletResponse response,
                            String query, String requestedPage, String role, User form,
                            Map<String, String> errors, Integer selectedPenghuniId)
            throws SQLException, ServletException, IOException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedRole = role == null ? "" : role.trim();
        if (!normalizedRole.isEmpty() && !ROLES.contains(normalizedRole)) {
            normalizedRole = "";
        }

        long totalItems = userDao.count(normalizedQuery, normalizedRole);
        Pagination pagination = Pagination.of(requestedPage, totalItems);
        List<UserDao.AccountRow> accounts = userDao.findPage(
                normalizedQuery, normalizedRole, pagination.getPageSize(), pagination.getOffset());

        Map<Integer, String> accountCreatedDates = new LinkedHashMap<>();
        for (UserDao.AccountRow account : accounts) {
            accountCreatedDates.put(account.getId(), account.getCreatedAt() == null
                    ? "-"
                    : CREATED_FORMAT.format(account.getCreatedAt()));
        }

        request.setAttribute("accountList", accounts);
        request.setAttribute("accountCreatedDates", accountCreatedDates);
        request.setAttribute("penghuniOptions", penghuniDao.findWithoutAccount());
        request.setAttribute("pagination", pagination);
        request.setAttribute("q", normalizedQuery);
        request.setAttribute("role", normalizedRole);
        request.setAttribute("form", form);
        request.setAttribute("selectedPenghuniId", selectedPenghuniId);
        request.setAttribute("errors", errors);
        request.setAttribute("currentUserId", currentUserId(request));
        request.setAttribute("pageTitle", "Akun");
        request.setAttribute("pageSubtitle", totalItems + " akun pengguna");
        request.setAttribute("pageSearchAction", request.getContextPath() + LIST_PATH);
        request.setAttribute("pageSearchPlaceholder", "Cari nama atau email");
        request.setAttribute("pageActionLabel", "Tambah akun");
        request.setAttribute("pageActionModal", "akun-create-modal");
        request.setAttribute("activeNav", "akun");
        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray());
    }

    private static boolean isValidPassword(String password) {
        return password.length() >= 8 && password.length() <= 72;
    }

    private static Integer currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null
                && session.getAttribute(AuthServlet.USER_SESSION_ATTRIBUTE) instanceof User user) {
            return user.getId();
        }
        return null;
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

    private static void redirectToList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + LIST_PATH);
    }

    private record FormValidation(User form, Map<String, String> errors, String password,
                                   Integer selectedPenghuniId) {
    }
}
