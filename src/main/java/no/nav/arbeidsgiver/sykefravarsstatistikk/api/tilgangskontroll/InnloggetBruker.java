package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Fnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnOrganisasjon;

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
