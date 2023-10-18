package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class KafkaUtsendingRapport {
    private val log = LoggerFactory.getLogger(this::class.java)

    val antallMeldingerMottattForUtsending: AtomicInteger = AtomicInteger()
    val antallMeldingerSent: AtomicInteger = AtomicInteger()
    val antallMeldingerIError: AtomicInteger = AtomicInteger()
    private val meldinger: MutableList<String> = ArrayList()
    private val sentVirksomheter // TODO: delete me --> not in use
            : MutableList<Orgnr> = ArrayList()
    private val ikkeSentVirksomheter // TODO: delete me --> not in use
            : MutableList<Orgnr> = ArrayList()
    private val antallUtsendigerMålet: AtomicInteger = AtomicInteger()
    private val antallDBOppdateringerMålet: AtomicInteger = AtomicInteger()
    private val totaltTidUtsendingTilKafka: AtomicLong = AtomicLong()
    private val totaltTidOppdaterDB: AtomicLong = AtomicLong()
    private var totalMeldingerTilUtsending = 0

    fun reset(totalMeldingerTilUtsending: Int) {
        this.totalMeldingerTilUtsending = totalMeldingerTilUtsending
        antallMeldingerMottattForUtsending.set(0)
        antallMeldingerIError.set(0)
        antallMeldingerSent.set(0)
        meldinger.clear()
        sentVirksomheter.clear()
        ikkeSentVirksomheter.clear()
        antallUtsendigerMålet.set(0)
        antallDBOppdateringerMålet.set(0)
        totaltTidUtsendingTilKafka.set(0)
        totaltTidOppdaterDB.set(0)
    }

    fun leggTilMeldingMottattForUtsending() {
        antallMeldingerMottattForUtsending.incrementAndGet()
    }

    fun leggTilUtsendingSuksess(orgnr: Orgnr) {
        sentVirksomheter.add(orgnr)
        leggTilUtsendingSuksess()
    }

    fun leggTilUtsendingSuksess() {
        antallMeldingerSent.incrementAndGet()
        loggVedSisteMelding()
    }

    fun leggTilError(errorMelding: String, orgnr: Orgnr) {
        ikkeSentVirksomheter.add(orgnr)
        leggTilError(errorMelding)
    }

    fun leggTilError(errorMelding: String) {
        antallMeldingerIError.incrementAndGet()
        meldinger.add(errorMelding)
        loggVedSisteMelding()
    }

    private fun loggVedSisteMelding() {
        val erSisteMelding = antallMeldingerSent.get() + antallMeldingerIError.get() == totalMeldingerTilUtsending
        if (erSisteMelding) {
            log.info(
                "Siste meldingen er sent. '{}' meldinger er bekreftet sent. '{}' meldinger i error. ",
                antallMeldingerSent.get(),
                antallMeldingerIError.get()
            )
        }
    }

    val snittTidUtsendingTilKafka: Long
        get() = if (antallUtsendigerMålet.get() == 0) {
            0
        } else totaltTidUtsendingTilKafka.get() / antallUtsendigerMålet.get()
    val snittTidOppdateringIDB: Long
        get() = if (antallDBOppdateringerMålet.get() == 0) {
            0
        } else totaltTidOppdaterDB.get() / antallDBOppdateringerMålet.get()
    val råDataVedDetaljertMåling: String
        get() = String.format(
            "Antall målet er: '%d', totaltTidUtsendingTilKafka er '%d' (in millis), "
                    + "totaltTidOppdaterDB er '%d' (in millis)",
            antallUtsendigerMålet.get(),
            totaltTidUtsendingTilKafka.get() / 1000000,
            totaltTidOppdaterDB.get() / 1000000
        )

    fun addUtsendingTilKafkaProcessingTime(
        startUtsendingProcess: Long, stopUtsendingProcess: Long
    ) {
        antallUtsendigerMålet.incrementAndGet()
        totaltTidUtsendingTilKafka.addAndGet(stopUtsendingProcess - startUtsendingProcess)
    }

    fun addDBOppdateringProcessingTime(
        startDBOppdateringProcess: Long, stopDBOppdateringProcess: Long
    ) {
        antallDBOppdateringerMålet.incrementAndGet()
        totaltTidOppdaterDB.addAndGet(stopDBOppdateringProcess - startDBOppdateringProcess)
    }
}
