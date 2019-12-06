package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import lombok.extern.slf4j.Slf4j;
import no.nav.metrics.MetricsFactory;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent.MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;

@Slf4j
@Component
public class BesøksstatistikkEventListener {

    private final BesøksstatistikkRepository besøksstatistikkRepository;

    public BesøksstatistikkEventListener(BesøksstatistikkRepository besøksstatistikkRepository) {
        this.besøksstatistikkRepository = besøksstatistikkRepository;
    }

    @EventListener
    public void onSammenligningUtsendt(SammenligningEvent sammenligningEvent) {
        String sessionId = sammenligningEvent.getSessionId();
        Underenhet underenhet = sammenligningEvent.getUnderenhet();

        if (besøksstatistikkRepository.sessionHarBlittRegistrert(sessionId, underenhet.getOrgnr())) {
            return;
        }

        if (kanLagreBesøksdata(sammenligningEvent)) {

            Sammenligning sammenligning = sammenligningEvent.getSammenligning();
            Sykefraværprosent virksomhet = sammenligning.getVirksomhet();
            Næring næring2siffer = sammenligningEvent.getNæring2siffer();
            Enhet enhet = sammenligningEvent.getEnhet();
            Sektor ssbSektor = sammenligningEvent.getSsbSektor();

            besøksstatistikkRepository.lagreBesøkFraStorVirksomhet(
                    sammenligningEvent.getEnhet(),
                    underenhet,
                    sammenligningEvent.getSsbSektor(),
                    sammenligningEvent.getNæring5siffer(),
                    næring2siffer,
                    sammenligning,
                    sammenligningEvent.getSessionId()
            );

            MetricsFactory.createEvent("sykefravarsstatistikk.stor-bedrift.besok")
                    .addTagToReport("arstall", String.valueOf(sammenligning.getÅrstall()))
                    .addTagToReport("kvartal", String.valueOf(sammenligning.getKvartal()))
                    .addTagToReport("sykefravarsprosent", String.valueOf(virksomhet.getProsent()))
                    .addTagToReport("sykefravarsprosent_antall_personer", String.valueOf(virksomhet.getAntallPersoner()))
                    .addTagToReport("naring_2siffer_sykefravarsprosent", String.valueOf(sammenligning.getNæring().getProsent()))
                    .addTagToReport("ssb_sektor_sykefravarsprosent", String.valueOf(sammenligning.getSektor().getProsent()))
                    .addTagToReport("antall_ansatte", String.valueOf(sammenligningEvent.getUnderenhet().getAntallAnsatte()))
                    .addTagToReport("naring_5siffer_kode", underenhet.getNæringskode().getKode())
                    .addTagToReport("naring_5siffer_beskrivelse", underenhet.getNæringskode().getBeskrivelse())
                    .addTagToReport("naring_2siffer_beskrivelse", næring2siffer.getNavn())
                    .addTagToReport("institusjonell_sektor_kode", enhet.getInstitusjonellSektorkode().getKode())
                    .addTagToReport("institusjonell_sektor_beskrivelse", enhet.getInstitusjonellSektorkode().getBeskrivelse())
                    .addTagToReport("ssb_sektor_kode", ssbSektor.getKode())
                    .addTagToReport("ssb_sektor_beskrivelse", ssbSektor.getNavn())
                    .report();

        } else {
            besøksstatistikkRepository.lagreBesøkFraLitenVirksomhet(sammenligningEvent.getSessionId());

            MetricsFactory.createEvent("sykefravarsstatistikk.liten-bedrift.besok").report();
        }
    }

    private boolean kanLagreBesøksdata(SammenligningEvent event) {
        return !event.getSammenligning().getVirksomhet().isErMaskert()
                && event.getUnderenhet().getAntallAnsatte() >= MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
    }
}
