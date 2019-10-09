package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Sporbarhetslogg {

    private static final Logger SPORBARHETSLOGGER = LoggerFactory.getLogger("auditLogger");

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
        statements.add("fnr: " + innloggetBruker.getFnr());
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
        log.info(loggmelding);
    }
}
