package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.springframework.stereotype.Component;

@Component
public class SammenligningService {

    private final SammenligningRepository sykefravarprosentRepository;
    private final EnhetsregisteretClient enhetsregisteretClient;

    private static int ARSTALL = 2019;
    private static int KVARTAL = 1;

    public SammenligningService(
            SammenligningRepository sykefravarprosentRepository,
            EnhetsregisteretClient enhetsregisteretClient
    ) {
        this.sykefravarprosentRepository = sykefravarprosentRepository;
        this.enhetsregisteretClient = enhetsregisteretClient;
    }

    public Sammenligning hentSammenligningForUnderenhet(
            Orgnr orgnr
    ) {
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        Enhet enhet = enhetsregisteretClient.hentInformasjonOmEnhet(underenhet.getOverordnetEnhetOrgnr());

        // TODO Disse må diskuteres. Sektor fra brreg samsvarer ikke med dvh-sektor. Må også finne ut av hvor mange siffer vi skal ha.
        String sektor = enhet.getInstitusjonellSektorkode().getKode();
        String næring2siffer = underenhet.getNæringskode().hentNæringskode2Siffer();

        return new Sammenligning(
                KVARTAL,
                ARSTALL,
                sykefravarprosentRepository.hentSykefraværprosentVirksomhet(ARSTALL, KVARTAL, orgnr.getVerdi()),
                sykefravarprosentRepository.hentSykefraværprosentSektor(ARSTALL, KVARTAL, sektor),
                sykefravarprosentRepository.hentSykefraværprosentNæring(ARSTALL, KVARTAL, næring2siffer),
                sykefravarprosentRepository.hentSykefraværprosentLand(ARSTALL, KVARTAL)
        );
    }
}
