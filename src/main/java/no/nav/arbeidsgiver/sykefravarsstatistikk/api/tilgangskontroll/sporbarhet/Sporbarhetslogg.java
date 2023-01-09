package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.sporbarhet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.CorrelationIdFilter.CORRELATION_ID_MDC_NAME;

@Component
public class Sporbarhetslogg {

  private static final Logger SPORBARHETSLOGGER = LoggerFactory.getLogger("sporbarhetslogger");

  public void loggHendelse(Loggevent event, String kommentar) {
    List<String> extensions = getExtensions(event);
    extensions.add("cs5=" + kommentar);
    extensions.add("cs5Label=Kommentar");

    String loggmelding = lagLoggmelding(extensions, event.isHarTilgang());

    if (event.isHarTilgang()) {
      SPORBARHETSLOGGER.info(loggmelding);
    } else {
      SPORBARHETSLOGGER.warn(loggmelding);
    }
  }

  public void loggHendelse(Loggevent event) {
    String loggmelding = lagLoggmelding(getExtensions(event), event.isHarTilgang());

    if (event.isHarTilgang()) {
      SPORBARHETSLOGGER.info(loggmelding);
    } else {
      SPORBARHETSLOGGER.warn(loggmelding);
    }
  }

  private String lagLoggmelding(List<String> extensions, boolean harTilgang) {
    String version = "CEF:0";
    String deviceVendor = "sykefravarsstatistikk-api";
    String deviceProduct = "sporbarhetslogg";
    String deviceVersion = "1.0";
    String signatureID = "sykefravarsstatistikk-api:accessed";
    String name = "sykefravarsstatistikk";
    String severity = harTilgang ? "INFO" : "WARN";

    String extension = String.join(" ", extensions);

    return String.join(
        "|",
        Arrays.asList(
            version,
            deviceVendor,
            deviceProduct,
            deviceVersion,
            signatureID,
            name,
            severity,
            extension));
  }

  private List<String> getExtensions(Loggevent event) {
    List<String> extensions = new ArrayList<>();
    extensions.add("end=" + System.currentTimeMillis());
    extensions.add("suid=" + event.getInnloggetBruker().getFnr().getVerdi());
    extensions.add("request=" + event.getRequestUrl());
    extensions.add("requestMethod=" + event.getRequestMethod());
    extensions.add("cs3=" + event.getOrgnr().getVerdi());
    extensions.add("cs3Label=OrgNr");
    String decision = event.isHarTilgang() ? "Permit" : "Deny";
    extensions.add("flexString1=" + decision);
    extensions.add("flexString1Label=Decision");

    if (!event.isHarTilgang()) {
      extensions.add("flexString2=Bruker har ikke rettighet i Altinn");
      extensions.add("flexString2Label=Begrunnelse");
      extensions.add("cn1=" + event.getAltinnServiceCode());
      extensions.add("cn1Label=Service Code");
      extensions.add("cn2=" + event.getAltinnServiceEdition());
      extensions.add("cn2Label=Service Edition");
    }

    extensions.add("sproc=" + MDC.get(CORRELATION_ID_MDC_NAME));
    return extensions;
  }
}
