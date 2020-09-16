package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.domene.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;

@AllArgsConstructor
@Data
public class Loggevent {
    private InnloggetBruker innloggetBruker;
    private Orgnr orgnr;
    private boolean harTilgang;
    private String requestMethod;
    private String requestUrl;
    private String altinnServiceCode;
    private String altinnServiceEdition;
}
