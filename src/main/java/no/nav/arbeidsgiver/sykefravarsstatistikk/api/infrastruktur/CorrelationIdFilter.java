package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.Konstanter.CORRELATION_ID_HEADER_NAME;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
  public static final String CORRELATION_ID_MDC_NAME = "correlationId";

  @Override
  protected void doFilterInternal(
          final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
      throws ServletException, IOException {
    try {
      String correlationIdHeader = request.getHeader(CORRELATION_ID_HEADER_NAME);

      if (isBlank(correlationIdHeader)) {
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
