package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk.SammenligningEvent;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.KlassifikasjonerRepository;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.SektorMappingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SammenligningService {

    private final SammenligningRepository sykefravarprosentRepository;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final SektorMappingService sektorMappingService;
    private final KlassifikasjonerRepository klassifikasjonerRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${statistikk.import.siste.arstall}")
    private int ARSTALL;
    @Value("${statistikk.import.siste.kvartal}")
    private int KVARTAL;

    public SammenligningService(
            SammenligningRepository sykefravarprosentRepository,
            EnhetsregisteretClient enhetsregisteretClient,
            SektorMappingService sektorMappingService,
            KlassifikasjonerRepository klassifikasjonerRepository,
            ApplicationEventPublisher eventPublisher) {
        this.sykefravarprosentRepository = sykefravarprosentRepository;
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.sektorMappingService = sektorMappingService;
        this.klassifikasjonerRepository = klassifikasjonerRepository;
        this.eventPublisher = eventPublisher;
    }

    public Sammenligning hentSammenligningForUnderenhet(
            Orgnr orgnr
    ) {
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        Enhet enhet = enhetsregisteretClient.hentInformasjonOmEnhet(underenhet.getOverordnetEnhetOrgnr());
        Sektor ssbSektor = sektorMappingService.mapTilSSBSektorKode(enhet.getInstitusjonellSektorkode());
        Næringskode5Siffer næring5siffer = underenhet.getNæringskode();
        Næring næring = klassifikasjonerRepository.hentNæring(næring5siffer.hentNæringskode2Siffer());
        Sammenligning sammenligning = new Sammenligning(
                KVARTAL,
                ARSTALL,
                sykefravarprosentRepository.hentSykefraværprosentVirksomhet(ARSTALL, KVARTAL, underenhet),
                sykefravarprosentRepository.hentSykefraværprosentNæring(ARSTALL, KVARTAL, næring),
                sykefravarprosentRepository.hentSykefraværprosentSektor(ARSTALL, KVARTAL, ssbSektor),
                sykefravarprosentRepository.hentSykefraværprosentLand(ARSTALL, KVARTAL)
        );

        eventPublisher.publishEvent(new SammenligningEvent(
                underenhet,
                enhet,
                ssbSektor,
                næring5siffer,
                næring,
                sammenligning
        ));

        return sammenligning;
    }
}
