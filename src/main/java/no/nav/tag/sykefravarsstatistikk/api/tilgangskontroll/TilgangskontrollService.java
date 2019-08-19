package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TilgangskontrollService {
    private final AltinnClient altinnClient;

    public TilgangskontrollService(AltinnClient altinnClient) {
        this.altinnClient = altinnClient;
    }

    public void sjekkTilgang(String fnr, String orgnr) {
        List<String> orgnumre = altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(fnr);
        if (orgnumre == null || !orgnumre.contains(orgnr)) {
            String feilmelding = "Bruker har ikke tilgang til IAWeb for orgnr=" + orgnr;
            log.error(feilmelding);
            throw new TilgangskontrollException(feilmelding);
        }
    }
}
