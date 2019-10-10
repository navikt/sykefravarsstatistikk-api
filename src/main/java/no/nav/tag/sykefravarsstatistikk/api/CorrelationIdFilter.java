package no.nav.tag.sykefravarsstatistikk.api;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String CORRELATION_ID_MDC_NAME = "correlationId";
    private static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws ServletException, IOException {
        try {
            String correlationIdHeader = request.getHeader(CORRELATION_ID_HEADER_NAME);

            if (correlationIdHeader == null || "".equals(correlationIdHeader)) {
                MDC.put(CORRELATION_ID_MDC_NAME, UUID.randomUUID().toString());
            } else {
                MDC.put(CORRELATION_ID_MDC_NAME, correlationIdHeader);
            }

            response.addHeader(CORRELATION_ID_HEADER_NAME, MDC.get(CORRELATION_ID_MDC_NAME));

            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_MDC_NAME);
        }
    }
}