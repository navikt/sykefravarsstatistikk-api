package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnClient;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TilgangskontrollService {

    private final AltinnClient altinnClient;
    private final TilgangskontrollUtils tokenUtils;

    public TilgangskontrollService(AltinnClient altinnClient, TilgangskontrollUtils tokenUtils) {
        this.altinnClient = altinnClient;
        this.tokenUtils = tokenUtils;
    }

    public InnloggetBruker hentInnloggetBruker() {
        if (tokenUtils.erInnloggetSelvbetjeningBruker()) {
            InnloggetBruker innloggetSelvbetjeningBruker = tokenUtils.hentInnloggetSelvbetjeningBruker();
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
