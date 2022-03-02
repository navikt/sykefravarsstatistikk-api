package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.oauth2.sdk.GeneralException;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnKlientWrapper;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.sporbarhet.Loggevent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.sporbarhet.Sporbarhetslogg;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.text.ParseException;

@Slf4j
@Component
public class TilgangskontrollService {

    private final AltinnKlientWrapper altinnKlientWrapper;
    private final TilgangskontrollUtils tokenUtils;
    private final Sporbarhetslogg sporbarhetslogg;
    private final String iawebServiceCode;
    private final String iawebServiceEdition;
    private final TokenXClient tokenXClient;

    public TilgangskontrollService(
            AltinnKlientWrapper altinnKlientWrapper,
            TilgangskontrollUtils tokenUtils,
            Sporbarhetslogg sporbarhetslogg,
            @Value("${altinn.iaweb.service.code}") String iawebServiceCode,
            @Value("${altinn.iaweb.service.edition}") String iawebServiceEdition,
            TokenXClient tokenXClient) {
        this.altinnKlientWrapper = altinnKlientWrapper;
        this.tokenUtils = tokenUtils;
        this.sporbarhetslogg = sporbarhetslogg;
        this.iawebServiceCode = iawebServiceCode;
        this.iawebServiceEdition = iawebServiceEdition;
        this.tokenXClient = tokenXClient;
    }

    public InnloggetBruker hentInnloggetBruker() {
        InnloggetBruker innloggetBruker = tokenUtils.hentInnloggetBruker();
        try {
            JwtToken exchangedTokenToAltinnProxy =
                    tokenXClient.exchangeTokenToAltinnProxy(tokenUtils.hentInnloggetJwtToken());
            innloggetBruker.setOrganisasjoner(
                    altinnKlientWrapper.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(
                            exchangedTokenToAltinnProxy,
                            innloggetBruker.getFnr()
                    )
            );
        } catch (ParseException | JOSEException | GeneralException | IOException | HttpClientErrorException e) {
            throw new TilgangskontrollException(e.getMessage());
        }
        return innloggetBruker;
    }

    public InnloggetBruker hentInnloggetBrukerForAlleRettigheter() {
        InnloggetBruker innloggetBruker = tokenUtils.hentInnloggetBruker();
        try {
            JwtToken exchangedTokenToAltinnProxy =
                    tokenXClient.exchangeTokenToAltinnProxy(tokenUtils.hentInnloggetJwtToken());

            innloggetBruker.setOrganisasjoner(
                    altinnKlientWrapper.hentOrgnumreDerBrukerHarTilgangTil(
                            exchangedTokenToAltinnProxy,
                            innloggetBruker.getFnr()
                    )
            );
        } catch (ParseException | JOSEException | GeneralException | IOException | HttpClientErrorException e) {
           throw new TilgangskontrollException(e.getMessage());
        }
        return innloggetBruker;
    }

    public boolean hentTilgangTilOverordnetEnhetOgLoggSikkerhetshendelse(
            InnloggetBruker bruker,
            OverordnetEnhet overordnetEnhet,
            Underenhet underenhet,
            String httpMetode,
            String requestUrl
    ) {
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
