package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static no.nav.tag.sykefravarsstatistikk.api.CorrelationIdFilter.CORRELATION_ID_MDC_NAME;

@Component
public class Sporbarhetslogg {

    private static final Logger SPORBARHETSLOGGER = LoggerFactory.getLogger("sporbarhetslogger");

    public void loggHendelse(
            InnloggetBruker innloggetBruker,
            Orgnr orgnr,
            boolean harTilgang,
            String requestMethod,
            String requestUrl,
            String altinnServiceCode,
            String altinnServiceEdition
    ) {
        String severity = harTilgang ? "INFO" : "WARN";

        List<String> statements = new ArrayList<>();
        statements.add("callId: " + MDC.get(CORRELATION_ID_MDC_NAME));
        statements.add("fnr: " + innloggetBruker.getFnr().getVerdi());
        statements.add("Method: " + requestMethod);
        statements.add("Endpoint: " + requestUrl);
        statements.add("orgnr: " + orgnr.getVerdi());
        statements.add("harTilgang: " + harTilgang);

        if (!harTilgang) {
            statements.add(
                    "Begrunnelse: Bruker har ikke rettigheten i Altinn spesifisert av service code "
                            + altinnServiceCode
                            + " og service edition "
                            + altinnServiceEdition
            );
        }

        String loggmelding = severity + " " + String.join("; ", statements);

        if (harTilgang) {
            SPORBARHETSLOGGER.info(loggmelding);
        } else {
            SPORBARHETSLOGGER.warn(loggmelding);
        }
    }
}
