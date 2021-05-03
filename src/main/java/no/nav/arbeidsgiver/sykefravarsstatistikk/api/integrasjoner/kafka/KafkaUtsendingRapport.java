package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class KafkaUtsendingRapport {

    private final AtomicInteger antallMeldingerMottattForUtsending;
    private final AtomicInteger antallMeldingerSent;
    private final AtomicInteger antallMeldingerIError;
    private final List<String> meldinger;
    private final List<Orgnr> sentVirksomheter;
    private final List<Orgnr> ikkeSentVirksomheter;

    public KafkaUtsendingRapport() {
        antallMeldingerMottattForUtsending = new AtomicInteger();
        antallMeldingerIError = new AtomicInteger();
        antallMeldingerSent = new AtomicInteger();
        meldinger = new ArrayList<>();
        sentVirksomheter = new ArrayList<>();
        ikkeSentVirksomheter = new ArrayList<>();
    }

    public void reset() {
        antallMeldingerMottattForUtsending.set(0);
        antallMeldingerIError.set(0);
        antallMeldingerSent.set(0);
        meldinger.clear();
        sentVirksomheter.clear();
        ikkeSentVirksomheter.clear();
    }

    public void leggTilMeldingMottattForUtsending() {
        antallMeldingerMottattForUtsending.incrementAndGet();
    }

    public void leggTilUtsendingSuksess(Orgnr orgnr) {
        antallMeldingerSent.incrementAndGet();
        sentVirksomheter.add(orgnr);

        loggVedSisteMelding();
    }

    public void leggTilError(String errorMelding, Orgnr orgnr) {
        antallMeldingerIError.incrementAndGet();
        ikkeSentVirksomheter.add(orgnr);
        meldinger.add(errorMelding);

        loggVedSisteMelding();
    }

    private void loggVedSisteMelding() {
        boolean erSisteMelding =
                (antallMeldingerSent.get() + antallMeldingerIError.get()) == antallMeldingerMottattForUtsending.get();

        if (erSisteMelding) {
            log.info("Siste meldingen er sent. '{}' meldinger er bekreftet sent. '{}' meldinger i error. ",
                    antallMeldingerSent.get(),
                    antallMeldingerIError.get()
            );
        }
    }


    public int getAntallMeldingerMottattForUtsending() {
        return antallMeldingerMottattForUtsending.get();
    }

    public int getAntallMeldingerSent() {
        return antallMeldingerSent.get();
    }

    public int getAntallMeldingerIError() {
        return antallMeldingerIError.get();
    }
}
