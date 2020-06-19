package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.security.oidc.api.Unprotected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Optional;

@Unprotected
@RestController
public class BedriftsmetrikkerController {
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final Bransjeprogram bransjeprogram;

    private static Logger logger = LoggerFactory.getLogger(ResponseEntityExceptionHandler.class);

    public BedriftsmetrikkerController(
            EnhetsregisteretClient enhetsregisteretClient,
            Bransjeprogram bransjeprogram) {
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.bransjeprogram = bransjeprogram;
    }

    @GetMapping(value = "/{orgnr}/bedriftsmetrikker")
    public Bedriftsmetrikker hentBedriftsmetrikker(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {

        try {
            Orgnr orgnr = new Orgnr(orgnrStr);
            Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
            Optional<Bransje> bransje = bransjeprogram.finnBransje(underenhet);

            return Bedriftsmetrikker.builder()
                    .antallAnsatte(new BigDecimal(underenhet.getAntallAnsatte()))
                    .næringskode5Siffer(underenhet.getNæringskode())
                    .bransje(bransje.isPresent() ? bransje.get().getType() : null)
                    .build();
        } catch (Exception e) {
            logger.warn("Feil ved kall til Enhetsregisteret");
            return null;
        }
    }
}
