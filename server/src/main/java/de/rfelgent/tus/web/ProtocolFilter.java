package de.rfelgent.tus.web;

import de.rfelgent.tus.TusHeaders;
import de.rfelgent.tus.TusVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author rfelgentraeger
 */
public class ProtocolFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolFilter.class);

    /** the supported version */
    private String version = TusVersion.SEMVERSION_1_0_0;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse resp = (HttpServletResponse)response;

        String versionHeader = req.getHeader(TusHeaders.TUS_RESUMABLE);
        if (versionHeader == null ||
                !TusVersion.SEMVERSION_1_0_0.equalsIgnoreCase(version)) {

            resp.setHeader(TusHeaders.TUS_VERSION, version);
            resp.setStatus(HttpStatus.PRECONDITION_FAILED.value());
            LOGGER.warn("Canceling processing request, as the version {} is not supported", versionHeader);
            return;
        }
        LOGGER.debug("Version {} supported. Continue to process request", versionHeader);
        try {
            chain.doFilter(request, response);
        } finally {
            if (!"OPTIONS".equalsIgnoreCase(req.getMethod())) {
                resp.setHeader(TusHeaders.TUS_RESUMABLE, version);
            }
        }

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
