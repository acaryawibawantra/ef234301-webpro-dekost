package id.dekost.servlet;

import id.dekost.model.User;
import id.dekost.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/logout"})
public class AuthServlet extends HttpServlet {

    public static final String USER_SESSION_ATTRIBUTE = "user";
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;
    private static final String ADMIN_HOME = "/admin/dashboard";
    private static final String RESIDENT_HOME = "/penghuni/dashboard";

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/logout".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "Logout harus menggunakan POST.");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/public/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/logout".equals(request.getServletPath())) {
            logout(request, response);
            return;
        }

        Optional<User> authenticatedUser;
        try {
            authenticatedUser = authService.authenticate(
                    request.getParameter("email"),
                    request.getParameter("password")
            );
        } catch (SQLException e) {
            throw new ServletException("Gagal memverifikasi akun.", e);
        }

        if (authenticatedUser.isEmpty()) {
            request.setAttribute("error", "Email atau password tidak valid.");
            request.getRequestDispatcher("/WEB-INF/views/public/login.jsp").forward(request, response);
            return;
        }

        HttpSession previousSession = request.getSession(false);
        if (previousSession != null) {
            previousSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_SESSION_ATTRIBUTE, authenticatedUser.get());
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

        String destination;
        if ("ADMIN".equals(authenticatedUser.get().getRole())) {
            destination = ADMIN_HOME;
        } else if ("PENGHUNI".equals(authenticatedUser.get().getRole())) {
            destination = RESIDENT_HOME;
        } else {
            destination = "/";
        }
        response.sendRedirect(request.getContextPath() + destination);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
