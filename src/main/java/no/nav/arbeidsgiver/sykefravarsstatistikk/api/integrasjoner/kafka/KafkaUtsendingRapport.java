package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class KafkaUtsendingRapport {

    private final AtomicInteger antallMeldingerMottattForUtsending;
    private final AtomicInteger antallMeldingerSent;
    private final AtomicInteger antallMeldingerIError;
    private final List<String> meldinger;
    private final List<Orgnr> sentVirksomheter;
    private final List<Orgnr> ikkeSentVirksomheter;
    private AtomicInteger antallMålet;
    private AtomicLong totaltTidUtsendingTilKafka;
    private AtomicLong totaltTidOppdaterDB;
    private int totalMeldingerTilUtsending;



    public KafkaUtsendingRapport() {
        antallMeldingerMottattForUtsending = new AtomicInteger();
        antallMeldingerIError = new AtomicInteger();
        antallMeldingerSent = new AtomicInteger();
        meldinger = new ArrayList<>();
        sentVirksomheter = new ArrayList<>();
        ikkeSentVirksomheter = new ArrayList<>();
        antallMålet = new AtomicInteger();
        totaltTidUtsendingTilKafka = new AtomicLong();
        totaltTidOppdaterDB = new AtomicLong();
    }

    public void reset(int totalMeldingerTilUtsending) {
        this.totalMeldingerTilUtsending = totalMeldingerTilUtsending;
        antallMeldingerMottattForUtsending.set(0);
        antallMeldingerIError.set(0);
        antallMeldingerSent.set(0);
        meldinger.clear();
        sentVirksomheter.clear();
        ikkeSentVirksomheter.clear();
        antallMålet.set(0);
        totaltTidUtsendingTilKafka.set(0);
        totaltTidOppdaterDB.set(0);
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
                (antallMeldingerSent.get() + antallMeldingerIError.get()) == totalMeldingerTilUtsending;

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

    public long getSnittTidUtsendingTilKafka() {
        if (antallMålet.get() == 0) {
            return 0;
        }

        return totaltTidUtsendingTilKafka.get() / antallMålet.get();
    }

    public long getSnittTidOppdateringIDB() {
        if (antallMålet.get() == 0) {
            return 0;
        }

        return totaltTidOppdaterDB.get() / antallMålet.get();
    }

    public String getRåDataVedDetaljertMåling() {
        return String.format(
                "Antall målet er: '%d', totaltTidUtsendingTilKafka er '%d', totaltTidOppdaterDB er '%d'",
                antallMålet.get(),
                totaltTidUtsendingTilKafka.get(),
                totaltTidOppdaterDB.get()
        );
    }

    public void addProcessingTime(
            long startUtsendingProcess,
            long stopUtsendingProcess,
            long startWriteToDb,
            long stoptWriteToDb
    ) {
        antallMålet.incrementAndGet();
        totaltTidUtsendingTilKafka.addAndGet(stopUtsendingProcess - startUtsendingProcess);
        totaltTidOppdaterDB.addAndGet(stoptWriteToDb - startWriteToDb);
    }
}
