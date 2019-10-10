package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnClient;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static no.nav.tag.sykefravarsstatistikk.api.sammenligning.SammenligningController.CORRELATION_ID;

@Slf4j
@Component
public class TilgangskontrollService {

    private final AltinnClient altinnClient;
    private final TilgangskontrollUtils tokenUtils;
    private final Sporbarhetslogg sporbarhetslogg;

    private final String iawebServiceCode;
    private final String iawebServiceEdition;

    public TilgangskontrollService(
            AltinnClient altinnClient,
            TilgangskontrollUtils tokenUtils,
            Sporbarhetslogg sporbarhetslogg,
            @Value("${altinn.iaweb.service.code}") String iawebServiceCode,
            @Value("${altinn.iaweb.service.edition}") String iawebServiceEdition
    ) {
        this.altinnClient = altinnClient;
        this.tokenUtils = tokenUtils;
        this.sporbarhetslogg = sporbarhetslogg;

        this.iawebServiceCode = iawebServiceCode;
        this.iawebServiceEdition = iawebServiceEdition;
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

    // TODO test dette
    public void sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(Orgnr orgnr, String httpMetode, String requestUrl) {
        InnloggetBruker bruker = hentInnloggetBruker();
        boolean harTilgang = bruker.harTilgang(orgnr);
        String callId = MDC.get(CORRELATION_ID);

        sporbarhetslogg.loggHendelse(
                callId,
                bruker,
                orgnr,
                harTilgang,
                httpMetode,
                requestUrl,
                iawebServiceCode,
                iawebServiceEdition
        );

        if (!harTilgang) {
            throw new TilgangskontrollException("Har ikke tilgang til statistikk for denne bedriften.");
        }
    }

}
