package club.lvivjava.kalah.adapter.in.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * MVP: {@code Authorization: Bearer <uuid>} identifies the caller. Replace with JWT in production.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BearerUserIdFilter extends OncePerRequestFilter {

    private static final Pattern BEARER = Pattern.compile("^Bearer\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || auth.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":{\"code\":\"unauthorized\",\"message\":\"Missing Authorization header\"}}");
            return;
        }
        var m = BEARER.matcher(auth.trim());
        if (!m.matches()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":{\"code\":\"unauthorized\",\"message\":\"Invalid Authorization scheme\"}}");
            return;
        }
        try {
            UUID id = UUID.fromString(m.group(1).trim());
            request.setAttribute(SecurityConstants.USER_ID_ATTRIBUTE, id);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":{\"code\":\"unauthorized\",\"message\":\"Bearer token must be a UUID\"}}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
