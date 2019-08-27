package no.nav.tag.sykefravarsstatistikk.api.domain.autorisasjon;

import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domain.Identifikator;
import no.nav.tag.sykefravarsstatistikk.api.domain.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;

@Data
public abstract class InnloggetBruker<T extends Identifikator> {
    private final T identifikator;


    protected abstract boolean harTilgang(Orgnr orgnr);

    public void sjekkTilgang(Orgnr orgnr) {
        if (!harTilgang(orgnr)) {
            throw new TilgangskontrollException("Har ikke tilgang til statistikk for denne bedriften.");
        }
    }
}
