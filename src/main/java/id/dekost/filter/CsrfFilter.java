package id.dekost.filter;

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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@WebFilter(filterName = "CsrfFilter", urlPatterns = "/*")
public class CsrfFilter implements Filter {

    public static final String REQUEST_ATTRIBUTE = "csrfToken";
    public static final String SESSION_ATTRIBUTE = CsrfFilter.class.getName() + ".token";
    public static final String PARAMETER_NAME = "_csrf";
    private static final String INVALID_TOKEN_MESSAGE =
            "Permintaan ditolak: token CSRF tidak ada atau tidak valid.";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(true);
        String token = getOrCreateToken(session);
        httpRequest.setAttribute(REQUEST_ATTRIBUTE, token);

        if ("POST".equalsIgnoreCase(httpRequest.getMethod())
                && !isValidToken(token, httpRequest.getParameter(PARAMETER_NAME))) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
            httpResponse.setContentType("text/plain");
            httpResponse.getWriter().write(INVALID_TOKEN_MESSAGE);
            return;
        }

        chain.doFilter(request, response);
    }

    private static String getOrCreateToken(HttpSession session) {
        synchronized (session) {
            Object existingToken = session.getAttribute(SESSION_ATTRIBUTE);
            if (existingToken instanceof String token) {
                return token;
            }

            byte[] randomBytes = new byte[32];
            RANDOM.nextBytes(randomBytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            session.setAttribute(SESSION_ATTRIBUTE, token);
            return token;
        }
    }

    private static boolean isValidToken(String expected, String provided) {
        return provided != null && MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8)
        );
    }
}
