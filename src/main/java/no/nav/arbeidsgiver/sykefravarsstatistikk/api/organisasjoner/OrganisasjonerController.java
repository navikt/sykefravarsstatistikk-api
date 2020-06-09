package no.nav.arbeidsgiver.sykefravarsstatistikk.api.organisasjoner;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.altinn.AltinnOrganisasjon;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.security.oidc.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Protected
@RestController
public class OrganisasjonerController {
    private final TilgangskontrollService tilgangskontrollService;

    public OrganisasjonerController(TilgangskontrollService tilgangskontrollService) {
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @GetMapping("/organisasjoner/statistikk")
    public List<AltinnOrganisasjon> hentOrganisasjonerMedStatistikktilgang() {
        InnloggetBruker innloggetBruker = tilgangskontrollService.hentInnloggetBruker();
        return innloggetBruker.getOrganisasjoner();
    }
}
