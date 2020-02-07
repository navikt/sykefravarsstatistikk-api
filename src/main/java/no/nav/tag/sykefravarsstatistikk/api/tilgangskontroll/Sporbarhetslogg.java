package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.*;

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
        long unixEpochTimestamp = System.currentTimeMillis() / 1000L;

        String version = "CEF:0";
        String deviceVendor = "sykefravarsstatistikk-api";
        String deviceProduct = "sporbarhetslogg";
        String deviceVersion = "1.0";
        String signatureID = "sykefravarsstatistikk-api:accessed";
        String name = "sykefravarsstatistikk";
        String severity = harTilgang ? "INFO" : "WARN";

        List<String> extensions = new ArrayList<>();
        extensions.add("end=" + unixEpochTimestamp);
        extensions.add("suid=" + innloggetBruker.getFnr().getVerdi());
        extensions.add("request=" + requestUrl);
        extensions.add("requestMethod=" + requestMethod);
        extensions.add("cs3=" + orgnr.getVerdi());
        extensions.add("cs3Label=OrgNr");
        extensions.add("flexString1=" + severity);
        extensions.add("flexString1Label=Decision");

        if (!harTilgang) {
            extensions.add("flexString2=Bruker har ikke rettighet i Altinn");
            extensions.add("flexString2Label=Begrunnelse");
            extensions.add("cn1=" + altinnServiceCode);
            extensions.add("cn1Label=Service Code");
            extensions.add("cn2=" + altinnServiceEdition);
            extensions.add("cn2Label=Service Edition");
        }

        extensions.add("sproc=" +  MDC.get(CORRELATION_ID_MDC_NAME));

        String extension = String.join(" ", extensions);

        String loggmelding = String.join("|", Arrays.asList(
                version,
                deviceVendor,
                deviceProduct,
                deviceVersion,
                signatureID,
                name,
                severity,
                extension
        ));

        if (harTilgang) {
            SPORBARHETSLOGGER.info(loggmelding);
        } else {
            SPORBARHETSLOGGER.warn(loggmelding);
        }
    }
}
