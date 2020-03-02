package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;

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
