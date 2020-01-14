package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import lombok.extern.slf4j.Slf4j;
import no.nav.metrics.MetricsFactory;
import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnClient;
import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnRole;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent.MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;

@Slf4j
@Component
public class BesøksstatistikkEventListener {

    private final BesøksstatistikkRepository besøksstatistikkRepository;
    private final AltinnClient altinnClient;

    public BesøksstatistikkEventListener(BesøksstatistikkRepository besøksstatistikkRepository, AltinnClient altinnClient) {
        this.besøksstatistikkRepository = besøksstatistikkRepository;
        this.altinnClient = altinnClient;
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
            lagreBesøksdataForStorVirksomhetIDatabase(sammenligningEvent);
            sendEventForStorVirksomhetTilInfluxDB(sammenligningEvent);
        } else {
            lagreBesøkForLitenVirksomhetIDatabase(sammenligningEvent);
            sendEventForLitenVirksomhetTilInfluxDB();
        }
        lagreRollerIDatabase(sammenligningEvent);
    }

    private void lagreRollerIDatabase(SammenligningEvent sammenligningEvent) {
        List<AltinnRole> altinnRoller = altinnClient.hentRollerBrukerHarIEnOrganisasjon(
                sammenligningEvent.getFnr(),
                sammenligningEvent.getUnderenhet().getOrgnr()
        );

        besøksstatistikkRepository.lagreRollerTilBesøkende(
                2020,
                2,
                altinnRoller
                        .stream()
                        .map( rolle -> rolle.getDefinitionId() )
                        .collect(Collectors.toList())
        );
    }

    private void lagreBesøkForLitenVirksomhetIDatabase(SammenligningEvent sammenligningEvent) {
        besøksstatistikkRepository.lagreBesøkFraLitenVirksomhet(sammenligningEvent.getSessionId());
    }

    private void lagreBesøksdataForStorVirksomhetIDatabase(SammenligningEvent sammenligningEvent) {
        besøksstatistikkRepository.lagreBesøkFraStorVirksomhet(
                sammenligningEvent.getEnhet(),
                sammenligningEvent.getUnderenhet(),
                sammenligningEvent.getSsbSektor(),
                sammenligningEvent.getNæring5siffer(),
                sammenligningEvent.getNæring2siffer(),
                sammenligningEvent.getSammenligning(),
                sammenligningEvent.getSessionId()
        );
    }

    private void sendEventForLitenVirksomhetTilInfluxDB() {
        MetricsFactory.createEvent("sykefravarsstatistikk.liten-bedrift.besok").report();
    }

    private void sendEventForStorVirksomhetTilInfluxDB(SammenligningEvent sammenligningEvent) {
        Sammenligning sammenligning = sammenligningEvent.getSammenligning();
        Enhet enhet = sammenligningEvent.getEnhet();
        Sektor ssbSektor = sammenligningEvent.getSsbSektor();
        Underenhet underenhet = sammenligningEvent.getUnderenhet();

        Optional<BigDecimal> prosentVirksomhet = Optional.ofNullable(sammenligning.getVirksomhet().getProsent());

        Optional<BigDecimal> sykefraværprosentNæring = Optional.ofNullable(sammenligning.getNæring()).map(Sykefraværprosent::getProsent);

        boolean virksomhetErOverSnittetINæringen;

        if (prosentVirksomhet.isEmpty() || sykefraværprosentNæring.isEmpty()) {
            virksomhetErOverSnittetINæringen = false;
        } else {
            virksomhetErOverSnittetINæringen = prosentVirksomhet.get().compareTo(sykefraværprosentNæring.get()) > 0;
        }

        MetricsFactory.createEvent("sykefravarsstatistikk.stor-bedrift.besok")
                .addTagToReport("arstall", String.valueOf(sammenligning.getÅrstall()))
                .addTagToReport("kvartal", String.valueOf(sammenligning.getKvartal()))
                .addTagToReport("naring_5siffer_kode", underenhet.getNæringskode().getKode())
                .addTagToReport("naring_5siffer_beskrivelse", underenhet.getNæringskode().getBeskrivelse())
                .addTagToReport("naring_2siffer_beskrivelse", sammenligningEvent.getNæring2siffer().getNavn())
                .addTagToReport("institusjonell_sektor_kode", enhet.getInstitusjonellSektorkode().getKode())
                .addTagToReport("institusjonell_sektor_beskrivelse", enhet.getInstitusjonellSektorkode().getBeskrivelse())
                .addTagToReport("ssb_sektor_kode", ssbSektor.getKode())
                .addTagToReport("ssb_sektor_beskrivelse", ssbSektor.getNavn())
                .addTagToReport("sykefravarsprosent_antall_personer", String.valueOf(sammenligning.getVirksomhet().getAntallPersoner()))
                .addTagToReport("naring_2siffer_sykefravarsprosent", String.valueOf(sykefraværprosentNæring.orElse(null)))
                .addTagToReport("ssb_sektor_sykefravarsprosent", String.valueOf(sammenligning.getSektor().getProsent()))
                .addTagToReport("sykefravarsprosent_over_naring_snitt", virksomhetErOverSnittetINæringen ? "true" : "false")

                .addFieldToReport("virksomhet_sykefravarsprosent", prosentVirksomhet.isPresent() ? prosentVirksomhet.get().floatValue() : null)
                .addFieldToReport("antall_ansatte", underenhet.getAntallAnsatte())

                .report();
    }

    private boolean kanLagreBesøksdata(SammenligningEvent event) {
        return !event.getSammenligning().getVirksomhet().isErMaskert()
                && event.getUnderenhet().getAntallAnsatte() >= MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
    }
}
