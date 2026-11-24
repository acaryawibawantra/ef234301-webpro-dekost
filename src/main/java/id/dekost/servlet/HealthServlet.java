package id.dekost.servlet;

import id.dekost.config.Database;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Halaman uji: memastikan aplikasi berjalan dan database dapat dijangkau.
 */
@WebServlet(name = "HealthServlet", urlPatterns = "/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean databaseConnected = Database.ping();

        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(databaseConnected
                ? HttpServletResponse.SC_OK
                : HttpServletResponse.SC_SERVICE_UNAVAILABLE);

        try (PrintWriter out = response.getWriter()) {
            out.println("OK");
            out.println("Database: " + (databaseConnected ? "terhubung" : "tidak terhubung"));
        }
    }
}
