package id.dekost.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Menampilkan halaman login. Proses autentikasi (POST) dikerjakan pada
 * task BE #9; saat ini hanya GET yang tersedia untuk kebutuhan UI.
 */
@WebServlet(name = "AuthServlet", urlPatterns = "/login")
public class AuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/public/login.jsp").forward(request, response);
    }
}
