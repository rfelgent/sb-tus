package de.rfelgent.tus.web;

import de.rfelgent.tus.TusHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author rfelgentraeger
 */
public class MethodOverrideFilter extends OncePerRequestFilter {

    private static class MethodRequestWrapper extends HttpServletRequestWrapper {

        public MethodRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getMethod() {
            String override = getHeader(TusHeaders.METHOD_OVERRIDE);
            if (override != null) {
                return override.toUpperCase();
            } else {
                return super.getMethod();
            }
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(new MethodRequestWrapper(request), response);
    }
}
