package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.altinn.AltinnService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TilgangskontrollService {

    private final AltinnService altinnService;
    private final TilgangskontrollUtils tokenUtils;
    private final Sporbarhetslogg sporbarhetslogg;

    private final String iawebServiceCode;
    private final String iawebServiceEdition;

    public TilgangskontrollService(
            AltinnService altinnService,
            TilgangskontrollUtils tokenUtils,
            Sporbarhetslogg sporbarhetslogg,
            @Value("${altinn.iaweb.service.code}") String iawebServiceCode,
            @Value("${altinn.iaweb.service.edition}") String iawebServiceEdition
    ) {
        this.altinnService = altinnService;
        this.tokenUtils = tokenUtils;
        this.sporbarhetslogg = sporbarhetslogg;

        this.iawebServiceCode = iawebServiceCode;
        this.iawebServiceEdition = iawebServiceEdition;
    }

    public InnloggetBruker hentInnloggetBruker() {
        if (tokenUtils.erInnloggetSelvbetjeningBruker()) {
            InnloggetBruker innloggetSelvbetjeningBruker = tokenUtils.hentInnloggetSelvbetjeningBruker();
            innloggetSelvbetjeningBruker.setOrganisasjoner(
                    altinnService.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(
                            innloggetSelvbetjeningBruker.getFnr()
                    )
            );
            return innloggetSelvbetjeningBruker;
        } else {
            throw new TilgangskontrollException("Innlogget bruker er ikke selvbetjeningsbruker");
        }
    }

    public boolean hentTilgangTilOverordnetEnhetOgLoggSikkerhetshendelse(
            OverordnetEnhet overordnetEnhet,
            Underenhet underenhet,
            String httpMetode,
            String requestUrl
    ) {
        InnloggetBruker bruker = hentInnloggetBruker();
        boolean harTilgang = bruker.harTilgang(overordnetEnhet.getOrgnr());
        String kommentar = String.format(
                "Bruker ba om tilgang orgnr %s indirekte ved å kalle endepunktet til underenheten %s",
                overordnetEnhet.getOrgnr().getVerdi(),
                underenhet.getOrgnr().getVerdi()
        );
        sporbarhetslogg.loggHendelse(new Loggevent(
                bruker,
                overordnetEnhet.getOrgnr(),
                harTilgang,
                httpMetode,
                requestUrl,
                iawebServiceCode,
                iawebServiceEdition
        ),
                kommentar);

        return harTilgang;
    }

    public void sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(Orgnr orgnr, String httpMetode, String requestUrl) {
        InnloggetBruker bruker = hentInnloggetBruker();
        sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(orgnr, bruker, httpMetode, requestUrl);
    }

    public void sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(Orgnr orgnr, InnloggetBruker bruker, String httpMetode, String requestUrl) {
        boolean harTilgang = bruker.harTilgang(orgnr);

        sporbarhetslogg.loggHendelse(new Loggevent(
                bruker,
                orgnr,
                harTilgang,
                httpMetode,
                requestUrl,
                iawebServiceCode,
                iawebServiceEdition
        ));

        if (!harTilgang) {
            throw new TilgangskontrollException("Har ikke tilgang til statistikk for denne bedriften.");
        }
    }

}
