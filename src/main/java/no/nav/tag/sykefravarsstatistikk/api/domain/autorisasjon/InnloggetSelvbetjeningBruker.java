package no.nav.tag.sykefravarsstatistikk.api.domain.autorisasjon;

import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.sykefravarsstatistikk.api.altinn.Organisasjon;
import no.nav.tag.sykefravarsstatistikk.api.domain.Fnr;
import no.nav.tag.sykefravarsstatistikk.api.domain.Orgnr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class InnloggetSelvbetjeningBruker extends InnloggetBruker<Fnr> {
    private List<Organisasjon> organisasjoner = new ArrayList<>();

    public InnloggetSelvbetjeningBruker(Fnr identifikator) {
        super(identifikator);
    }

    @Override
    protected boolean harTilgang(Orgnr orgnr) {
        Optional<Organisasjon> organisasjon = getOrganisasjoner()
                .stream()
                .filter(Objects::nonNull)
                .filter(
                        o -> o.getOrganizationNumber().equals(orgnr.getVerdi())
                )
                .findFirst();

        return organisasjon.isPresent();
    }
}
