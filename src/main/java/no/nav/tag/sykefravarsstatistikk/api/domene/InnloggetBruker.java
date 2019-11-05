package no.nav.tag.sykefravarsstatistikk.api.domene;

import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnOrganisasjon;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class InnloggetBruker {
    private List<AltinnOrganisasjon> organisasjoner;
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

    public boolean harTilgang(Orgnr orgnr) {
        List<String> orgnumreBrukerHarTilgangTil = organisasjoner
                .stream()
                .filter(Objects::nonNull)
                .map(org -> org.getOrganizationNumber())
                .collect(Collectors.toList());

        return orgnumreBrukerHarTilgangTil.contains(orgnr.getVerdi());
    }
}
