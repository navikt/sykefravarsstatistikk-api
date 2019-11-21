package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.SektorMappingService;
import org.springframework.stereotype.Component;

@Component
public class SammenligningService {

    private final SammenligningRepository sykefravarprosentRepository;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final SektorMappingService sektorMappingService;


    private static int ARSTALL = 2019;
    private static int KVARTAL = 1;

    public SammenligningService(
            SammenligningRepository sykefravarprosentRepository,
            EnhetsregisteretClient enhetsregisteretClient,
            SektorMappingService sektorMappingService) {
        this.sykefravarprosentRepository = sykefravarprosentRepository;
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.sektorMappingService = sektorMappingService;
    }

    public Sammenligning hentSammenligningForUnderenhet(
            Orgnr orgnr
    ) {
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        Enhet enhet = enhetsregisteretClient.hentInformasjonOmEnhet(underenhet.getOverordnetEnhetOrgnr());
        Sektor ssbSektor = sektorMappingService.mapTilSSBSektorKode(enhet.getInstitusjonellSektorkode());

        return new Sammenligning(
                KVARTAL,
                ARSTALL,
                sykefravarprosentRepository.hentSykefraværprosentVirksomhet(ARSTALL, KVARTAL, underenhet),
                sykefravarprosentRepository.hentSykefraværprosentNæring(ARSTALL, KVARTAL, underenhet.getNæringskode()),
                sykefravarprosentRepository.hentSykefraværprosentSektor(ARSTALL, KVARTAL, ssbSektor),
                sykefravarprosentRepository.hentSykefraværprosentLand(ARSTALL, KVARTAL)
        );
    }
}
