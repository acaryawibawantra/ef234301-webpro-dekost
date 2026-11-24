package id.dekost.filter;

import id.dekost.model.User;
import id.dekost.servlet.AuthServlet;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = "/admin/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        Object sessionUser = session == null
                ? null
                : session.getAttribute(AuthServlet.USER_SESSION_ATTRIBUTE);

        if (!(sessionUser instanceof User user)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        if (!"ADMIN".equals(user.getRole())) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Akun ini tidak memiliki akses ke area admin.");
            return;
        }

        chain.doFilter(request, response);
    }
}
