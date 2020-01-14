package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import lombok.extern.slf4j.Slf4j;
import no.nav.metrics.MetricsFactory;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

import static no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent.MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;

@Slf4j
@Component
public class BesøksstatistikkEventListener {

    private final BesøksstatistikkRepository besøksstatistikkRepository;

    public BesøksstatistikkEventListener(BesøksstatistikkRepository besøksstatistikkRepository) {
        this.besøksstatistikkRepository = besøksstatistikkRepository;
    }

    @Async
    @EventListener
    public void onSammenligningUtsendt(SammenligningEvent sammenligningEvent) {
        String sessionId = sammenligningEvent.getSessionId();
        Orgnr orgnr = sammenligningEvent.getUnderenhet().getOrgnr();

        if (besøksstatistikkRepository.sessionHarBlittRegistrert(sessionId, orgnr)) {
            return;
        }

        if (kanLagreBesøksdata(sammenligningEvent)) {
            besøksstatistikkRepository.lagreBesøkFraStorVirksomhet(sammenligningEvent);
            sendEventForStorVirksomhetTilInfluxDB(sammenligningEvent);
        } else {
            besøksstatistikkRepository.lagreBesøkFraLitenVirksomhet(sammenligningEvent.getSessionId());
            sendEventForLitenVirksomhetTilInfluxDB();
        }
    }

    private void sendEventForLitenVirksomhetTilInfluxDB() {
        MetricsFactory.createEvent("sykefravarsstatistikk.liten-bedrift.besok").report();
    }

    private void sendEventForStorVirksomhetTilInfluxDB(SammenligningEvent sammenligningEvent) {
        Sammenligning sammenligning = sammenligningEvent.getSammenligning();
        Enhet enhet = sammenligningEvent.getEnhet();
        Sektor ssbSektor = sammenligningEvent.getSsbSektor();
        Underenhet underenhet = sammenligningEvent.getUnderenhet();

        Optional<String> bransjenavn = Optional.ofNullable(sammenligningEvent.getBransje()).map(Bransje::getNavn);
        Optional<BigDecimal> prosentVirksomhet = Optional.ofNullable(sammenligning.getVirksomhet().getProsent());
        Optional<BigDecimal> sykefraværprosentNæring = Optional.ofNullable(sammenligning.getNæring()).map(Sykefraværprosent::getProsent);
        Optional<BigDecimal> sykefraværprosentBransje = Optional.ofNullable(sammenligning.getBransje()).map(Sykefraværprosent::getProsent);

        boolean virksomhetErOverSnittetINæringenEllerBransjen = false;

        if (prosentVirksomhet.isPresent()) {
            if (sykefraværprosentNæring.isPresent()) {
                virksomhetErOverSnittetINæringenEllerBransjen = prosentVirksomhet.get().compareTo(sykefraværprosentNæring.get()) > 0;
            } else if (sykefraværprosentBransje.isPresent()) {
                virksomhetErOverSnittetINæringenEllerBransjen = prosentVirksomhet.get().compareTo(sykefraværprosentBransje.get()) > 0;
            }
        }

        MetricsFactory.createEvent("sykefravarsstatistikk.stor-bedrift.besok")
                .addTagToReport("arstall", String.valueOf(sammenligning.getÅrstall()))
                .addTagToReport("kvartal", String.valueOf(sammenligning.getKvartal()))
                .addTagToReport("naring_2siffer_kode", sammenligningEvent.getNæring2siffer().getKode())
                .addTagToReport("naring_2siffer_beskrivelse", sammenligningEvent.getNæring2siffer().getNavn())
                .addTagToReport("bransje_navn", bransjenavn.orElse(null))
                .addTagToReport("institusjonell_sektor_kode", enhet.getInstitusjonellSektorkode().getKode())
                .addTagToReport("institusjonell_sektor_beskrivelse", enhet.getInstitusjonellSektorkode().getBeskrivelse())
                .addTagToReport("ssb_sektor_kode", ssbSektor.getKode())
                .addTagToReport("ssb_sektor_beskrivelse", ssbSektor.getNavn())
                .addTagToReport("sykefravarsprosent_antall_personer", String.valueOf(sammenligning.getVirksomhet().getAntallPersoner()))
                .addTagToReport("naring_2siffer_sykefravarsprosent", String.valueOf(sykefraværprosentNæring.orElse(null)))
                .addTagToReport("bransje_sykefravarsprosent", String.valueOf(sykefraværprosentBransje.orElse(null)))
                .addTagToReport("ssb_sektor_sykefravarsprosent", String.valueOf(sammenligning.getSektor().getProsent()))
                .addTagToReport("sykefravarsprosent_over_naring_snitt", virksomhetErOverSnittetINæringenEllerBransjen ? "true" : "false")

                .addFieldToReport("virksomhet_sykefravarsprosent", prosentVirksomhet.isPresent() ? prosentVirksomhet.get().floatValue() : null)
                .addFieldToReport("antall_ansatte", underenhet.getAntallAnsatte())

                .report();
    }

    private boolean kanLagreBesøksdata(SammenligningEvent event) {
        return !event.getSammenligning().getVirksomhet().isErMaskert()
                && event.getUnderenhet().getAntallAnsatte() >= MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
    }
}
