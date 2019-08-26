package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnClient;
import no.nav.tag.sykefravarsstatistikk.api.domain.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domain.autorisasjon.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domain.autorisasjon.InnloggetSelvbetjeningBruker;
import no.nav.tag.sykefravarsstatistikk.api.utils.TokenUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TilgangskontrollService {

    private final AltinnClient altinnClient;
    private final TokenUtils tokenUtils;

    public TilgangskontrollService(AltinnClient altinnClient, TokenUtils tokenUtils) {
        this.altinnClient = altinnClient;
        this.tokenUtils = tokenUtils;
    }

    public void sjekkTilgang(Orgnr orgnr) {
        hentInnloggetBruker().sjekkTilgang(orgnr);
    }


    private InnloggetBruker hentInnloggetBruker() {
        if (tokenUtils.erInnloggetSelvbetjeningBruker()) {
            InnloggetSelvbetjeningBruker innloggetSelvbetjeningBruker = tokenUtils.hentInnloggetSelvbetjeningBruker();
            innloggetSelvbetjeningBruker.setOrganisasjoner(
                    altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(
                            innloggetSelvbetjeningBruker.getIdentifikator()
                    )
            );
            return innloggetSelvbetjeningBruker;
        } else {
            return tokenUtils.hentInnloggetNavAnsatt();
        }
    }

}
