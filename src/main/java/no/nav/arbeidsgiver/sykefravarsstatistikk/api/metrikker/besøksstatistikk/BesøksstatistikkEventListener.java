package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnRolle;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;
import java.util.List;

import static java.time.ZonedDateTime.now;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter.MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;

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
            besøksstatistikkRepository.lagreBesøkFraStorVirksomhet(sammenligningEvent);
        } else {
            besøksstatistikkRepository.lagreBesøkFraLitenVirksomhet(sammenligningEvent.getSessionId());
        }
        lagreRollerIDatabase(sammenligningEvent);
    }

    private void lagreRollerIDatabase(SammenligningEvent sammenligningEvent) {
        List<AltinnRolle> altinnRoller = altinnClient.hentRoller(
                sammenligningEvent.getFnr(),
                sammenligningEvent.getUnderenhet().getOrgnr()
        );

        ZonedDateTime now = now();
        besøksstatistikkRepository.lagreRollerKnyttetTilBesøket(
                now.getYear(),
                now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
                altinnRoller);
    }

    private boolean kanLagreBesøksdata(SammenligningEvent event) {
        return !event.getSammenligning().getVirksomhet().isErMaskert()
                && event.getUnderenhet().getAntallAnsatte() >= MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
    }
}
