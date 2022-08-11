package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.organisasjoner;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnOrganisasjon;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
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
        InnloggetBruker innloggetBruker = tilgangskontrollService.hentBrukerKunIaRettigheter();
        return innloggetBruker.getOrganisasjoner();
    }
    @GetMapping("/organisasjoner")
    public List<AltinnOrganisasjon> hentOrganisasjonerMedAlleTilganger() {
        InnloggetBruker innloggetBruker = tilgangskontrollService.hentInnloggetBrukerForAlleRettigheter();
        return innloggetBruker.getOrganisasjoner();
    }
}
