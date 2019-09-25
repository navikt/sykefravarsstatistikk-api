package no.nav.tag.sykefravarsstatistikk.api.domene;

import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.altinn.Organisasjon;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
public class InnloggetBruker {
    private List<Organisasjon> organisasjoner;
    private Fnr fnr;

    public InnloggetBruker(Fnr fnr) {
        this.fnr = fnr;
        organisasjoner = new ArrayList<>();
    }

    public void sjekkTilgang(Orgnr orgnr) {
        if (!harTilgang(orgnr)) {
            throw new TilgangskontrollException("Har ikke tilgang til statistikk for denne bedriften.");
        }
    }

    protected boolean harTilgang(Orgnr orgnr) {
        Optional<Organisasjon> organisasjon = organisasjoner
                .stream()
                .filter(Objects::nonNull)
                .filter(
                        o -> o.getOrganizationNumber().equals(orgnr.getVerdi())
                )
                .findFirst();

        return organisasjon.isPresent();
    }
}
