package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void leggTilUtsending(Orgnr orgnr) {
        antallMeldingerSent.incrementAndGet();
        sentVirksomheter.add(orgnr);
    }

    public void leggTilError(String errorMelding, Orgnr orgnr) {
        antallMeldingerIError.incrementAndGet();
        ikkeSentVirksomheter.add(orgnr);
        meldinger.add(errorMelding);
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
