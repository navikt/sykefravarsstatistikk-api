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
    private AtomicInteger antallUtsendigerMålet;
    private AtomicInteger antallDBOppdateringerMålet;
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
        antallUtsendigerMålet = new AtomicInteger();
        antallDBOppdateringerMålet = new AtomicInteger();
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
        antallUtsendigerMålet.set(0);
        antallDBOppdateringerMålet.set(0);
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
        if (antallUtsendigerMålet.get() == 0) {
            return 0;
        }

        return totaltTidUtsendingTilKafka.get() / antallUtsendigerMålet.get();
    }

    public long getSnittTidOppdateringIDB() {
        if (antallDBOppdateringerMålet.get() == 0) {
            return 0;
        }

        return totaltTidOppdaterDB.get() / antallDBOppdateringerMålet.get();
    }

    public String getRåDataVedDetaljertMåling() {
        return String.format(
                "Antall målet er: '%d', totaltTidUtsendingTilKafka er '%d', totaltTidOppdaterDB er '%d'",
                antallUtsendigerMålet.get(),
                totaltTidUtsendingTilKafka.get(),
                totaltTidOppdaterDB.get()
        );
    }

    public void addUtsendingTilKafkaProcessingTime(
            long startUtsendingProcess,
            long stopUtsendingProcess
    ) {
        antallUtsendigerMålet.incrementAndGet();
        totaltTidUtsendingTilKafka.addAndGet(stopUtsendingProcess - startUtsendingProcess);
    }

    public void addDBOppdateringProcessingTime(
            long startDBOppdateringProcess,
            long stopDBOppdateringProcess
            ) {
        antallDBOppdateringerMålet.incrementAndGet();
        totaltTidOppdaterDB.addAndGet(stopDBOppdateringProcess - startDBOppdateringProcess);
    }
}
