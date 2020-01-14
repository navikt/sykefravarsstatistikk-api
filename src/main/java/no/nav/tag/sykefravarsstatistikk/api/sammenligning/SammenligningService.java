package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk.SammenligningEvent;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransjeprogram;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.KlassifikasjonerRepository;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.SektorMappingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SammenligningService {

    private final SammenligningRepository sammenligningRepository;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final SektorMappingService sektorMappingService;
    private final KlassifikasjonerRepository klassifikasjonerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Bransjeprogram bransjeprogram;

    @Value("${statistikk.import.siste.arstall}")
    private int ARSTALL;
    @Value("${statistikk.import.siste.kvartal}")
    private int KVARTAL;

    public SammenligningService(
            SammenligningRepository sammenligningRepository,
            EnhetsregisteretClient enhetsregisteretClient,
            SektorMappingService sektorMappingService,
            KlassifikasjonerRepository klassifikasjonerRepository,
            ApplicationEventPublisher eventPublisher,
            Bransjeprogram bransjeprogram
    ) {
        this.sammenligningRepository = sammenligningRepository;
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.sektorMappingService = sektorMappingService;
        this.klassifikasjonerRepository = klassifikasjonerRepository;
        this.eventPublisher = eventPublisher;
        this.bransjeprogram = bransjeprogram;
    }

    public Sammenligning hentSammenligningForUnderenhet(
            Orgnr orgnr,
            InnloggetBruker innloggetSelvbetjeningBruker,
            String sessionId
    ) {
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        Enhet enhet = enhetsregisteretClient.hentInformasjonOmEnhet(underenhet.getOverordnetEnhetOrgnr());
        Sektor ssbSektor = sektorMappingService.mapTilSSBSektorKode(enhet.getInstitusjonellSektorkode());
        Næringskode5Siffer næring5siffer = underenhet.getNæringskode();
        Næring næring = klassifikasjonerRepository.hentNæring(næring5siffer.hentNæringskode2Siffer());

        Sykefraværprosent sykefraværprosentNæring = null;
        Sykefraværprosent sykefraværprosentBransje = null;
        Optional<Bransje> bransje = bransjeprogram.finnBransje(underenhet);
        if (bransje.isPresent()) {
            sykefraværprosentBransje = sammenligningRepository.hentSykefraværprosentBransje(ARSTALL, KVARTAL, bransje.get());
        } else {
            sykefraværprosentNæring = sammenligningRepository.hentSykefraværprosentNæring(ARSTALL, KVARTAL, næring);
        }

        Sammenligning sammenligning = new Sammenligning(
                KVARTAL,
                ARSTALL,
                sammenligningRepository.hentSykefraværprosentVirksomhet(ARSTALL, KVARTAL, underenhet),
                sykefraværprosentNæring,
                sykefraværprosentBransje,
                sammenligningRepository.hentSykefraværprosentSektor(ARSTALL, KVARTAL, ssbSektor),
                sammenligningRepository.hentSykefraværprosentLand(ARSTALL, KVARTAL)
        );

        eventPublisher.publishEvent(new SammenligningEvent(
                underenhet,
                enhet,
                ssbSektor,
                næring5siffer,
                næring,
                bransje.orElse(null),
                innloggetSelvbetjeningBruker.getFnr(),
                sammenligning,
                sessionId
        ));

        return sammenligning;
    }
}
