package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnClient;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetSelvbetjeningBruker;
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

    public InnloggetSelvbetjeningBruker hentInnloggetBruker() {
        if (tokenUtils.erInnloggetSelvbetjeningBruker()) {
            InnloggetSelvbetjeningBruker innloggetSelvbetjeningBruker = tokenUtils.hentInnloggetSelvbetjeningBruker();
            innloggetSelvbetjeningBruker.setOrganisasjoner(
                    altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(
                            innloggetSelvbetjeningBruker.getFnr()
                    )
            );
            return innloggetSelvbetjeningBruker;
        } else {
            throw new TilgangskontrollException("Innlogget bruker er ikke selvbetjeningsbruker");
        }
    }

}
