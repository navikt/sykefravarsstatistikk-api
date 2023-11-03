package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværFlereKvartalerForEksport
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic.Companion.from
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.GradertStatistikkategoriKafkamelding
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.StatistikkategoriKafkamelding
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EksporteringPerStatistikkKategoriService(
    private val sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository,
    private val sykefraværStatistikkSektorRepository: SykefraværStatistikkSektorRepository,
    private val sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository,
    private val sykefravarStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository,
    private val sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository,
    private val sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository,
    private val kafkaClient: KafkaClient,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun eksporterPerStatistikkKategori(
        årstallOgKvartal: ÅrstallOgKvartal,
        statistikkategori: Statistikkategori
    ) {
        log.info(
            "Starter eksportering av kategori '{}' for årstall '{}' og kvartal '{}' på topic '{}'.",
            statistikkategori.name,
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            from(statistikkategori)?.navn
        )

        when (statistikkategori) {
            Statistikkategori.LAND -> eksporterSykefraværsstatistikkLand(årstallOgKvartal)
            Statistikkategori.SEKTOR -> eksporterSykefraværsstatistikkSektor(årstallOgKvartal)
            Statistikkategori.NÆRING -> eksporterSykefraværsstatistikkNæring(årstallOgKvartal)
            Statistikkategori.NÆRINGSKODE -> eksporterSykefraværsstatistikkNæringskode(årstallOgKvartal)
            Statistikkategori.BRANSJE -> eksporterSykefraværsstatistikkBransje(årstallOgKvartal)
            Statistikkategori.VIRKSOMHET -> eksporterSykefraværsstatistikkVirksomhet(årstallOgKvartal)
            Statistikkategori.VIRKSOMHET_GRADERT -> eksporterSykefraværsstatistikkVirksomhetGradert(årstallOgKvartal)
            Statistikkategori.OVERORDNET_ENHET -> log.warn(
                "Ikke implementert eksport for kategori '{}'",
                statistikkategori.name
            )
        }

        log.info("Eksportering av kategori '{}' er ferdig.", statistikkategori.name)
    }

    private fun eksporterSykefraværsstatistikkLand(årstallOgKvartal: ÅrstallOgKvartal) {

        sykefraværStatistikkLandRepository.hentSykefraværstatistikkLand(årstallOgKvartal inkludertTidligere 3)
            .groupByLand().let {
                eksporterSykefraværsstatistikkPerKategori(
                    årstallOgKvartal = årstallOgKvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.LAND,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_LAND_V1
                )
            }
    }

    private fun eksporterSykefraværsstatistikkSektor(årstallOgKvartal: ÅrstallOgKvartal) {
        sykefraværStatistikkSektorRepository.hentForKvartaler(årstallOgKvartal inkludertTidligere 3)
            .groupBySektor()
            .let {
                eksporterSykefraværsstatistikkPerKategori(
                    årstallOgKvartal = årstallOgKvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.SEKTOR,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_SEKTOR_V1
                )
            }
    }

    private fun eksporterSykefraværsstatistikkNæring(årstallOgKvartal: ÅrstallOgKvartal) {
        sykefraværStatistikkNæringRepository.hentForAlleNæringer(
            årstallOgKvartal inkludertTidligere 3
        ).groupByNæring().let {
            eksporterSykefraværsstatistikkPerKategori(
                årstallOgKvartal = årstallOgKvartal,
                sykefraværGruppertEtterKode = it,
                statistikkategori = Statistikkategori.NÆRING,
                kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_NARING_V1
            )
        }
    }

    private fun eksporterSykefraværsstatistikkNæringskode(årstallOgKvartal: ÅrstallOgKvartal) {
        sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(årstallOgKvartal inkludertTidligere 3)
            .groupByNæringskode().let {
                eksporterSykefraværsstatistikkPerKategori(
                    årstallOgKvartal = årstallOgKvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.NÆRINGSKODE,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_NARINGSKODE_V1
                )
            }
    }

    private fun eksporterSykefraværsstatistikkBransje(kvartal: ÅrstallOgKvartal) {

        hentSykefraværsstatistikkForBransjer(
            kvartaler = kvartal inkludertTidligere 3,
            sykefraværsstatistikkNæringRepository = sykefraværStatistikkNæringRepository,
            sykefraværStatistikkNæringskodeRepository = sykefraværStatistikkNæringskodeRepository
        )
            .groupByBransje().let {
                eksporterSykefraværsstatistikkPerKategori(
                    årstallOgKvartal = kvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.BRANSJE,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_BRANSJE_V1
                )
            }
    }

    private fun eksporterSykefraværsstatistikkVirksomhet(årstallOgKvartal: ÅrstallOgKvartal) {
        sykefravarStatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(årstallOgKvartal inkludertTidligere 3)
            .groupByOrgnr().let {
                eksporterSykefraværsstatistikkPerKategori(
                    årstallOgKvartal = årstallOgKvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.VIRKSOMHET,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1
                )
            }
    }

    private fun eksporterSykefraværsstatistikkVirksomhetGradert(årstallOgKvartal: ÅrstallOgKvartal) {
        sykefravarStatistikkVirksomhetGraderingRepository.hentSykefraværAlleVirksomheterGradert(
            årstallOgKvartal inkludertTidligere 3
        ).groupByVirksomhet().let {
            eksporterSykefraværsstatistikkPerKategori(
                årstallOgKvartal = årstallOgKvartal,
                sykefraværGruppertEtterKode = it,
                statistikkategori = Statistikkategori.VIRKSOMHET_GRADERT,
                kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_VIRKSOMHET_GRADERT_V1
            )
        }
    }


    private fun eksporterSykefraværsstatistikkPerKategori(
        årstallOgKvartal: ÅrstallOgKvartal,
        sykefraværGruppertEtterKode: Map<String, List<Sykefraværsstatistikk>>,
        statistikkategori: Statistikkategori,
        kafkaTopic: KafkaTopic
    ) {
        var antallStatistikkEksportert = 0
        sykefraværGruppertEtterKode.forEach { (kode, statistikk) ->
            val umaskertSykefraværsstatistikkSiste4Kvartaler =
                statistikk.tilUmaskertSykefraværForEttKvartal()

            val sykefraværMedKategoriSisteKvartal =
                umaskertSykefraværsstatistikkSiste4Kvartaler
                    .tilSykefraværMedKategoriSisteKvartal(statistikkategori, kode)

            if (årstallOgKvartal != sykefraværMedKategoriSisteKvartal.årstallOgKvartal) {
                return
            }

            val melding = when (statistikkategori) {
                Statistikkategori.LAND,
                Statistikkategori.SEKTOR,
                Statistikkategori.NÆRING,
                Statistikkategori.BRANSJE,
                Statistikkategori.OVERORDNET_ENHET,
                Statistikkategori.VIRKSOMHET,
                Statistikkategori.NÆRINGSKODE -> StatistikkategoriKafkamelding(
                    sykefraværMedKategoriSisteKvartal,
                    SykefraværFlereKvartalerForEksport(umaskertSykefraværsstatistikkSiste4Kvartaler)
                )

                Statistikkategori.VIRKSOMHET_GRADERT -> GradertStatistikkategoriKafkamelding(
                    sykefraværMedKategoriSisteKvartal,
                    SykefraværFlereKvartalerForEksport(umaskertSykefraværsstatistikkSiste4Kvartaler)
                )
            }
            kafkaClient.sendMelding(melding, kafkaTopic)
            antallStatistikkEksportert++
        }
        log.info(
            "Ferdig med utsending av alle meldinger til Kafka for statistikkategori ${statistikkategori.name}. " +
                    "Antall statistikk eksportert: $antallStatistikkEksportert"
        )
    }


    fun List<Sykefraværsstatistikk>.tilUmaskertSykefraværForEttKvartal() =
        this.map {
            when (it) {
                is SykefraværsstatistikkBransje,
                is SykefraværsstatistikkForNæring,
                is SykefraværsstatistikkForNæringskode,
                is SykefraværsstatistikkLand,
                is SykefraværsstatistikkNæringMedVarighet,
                is SykefraværsstatistikkSektor,
                is SykefraværsstatistikkVirksomhetUtenVarighet,
                is SykefraværsstatistikkVirksomhet -> UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(it.årstall, it.kvartal),
                    it.tapteDagsverk!!,
                    it.muligeDagsverk!!,
                    it.antallPersoner
                )

                is SykefraværsstatistikkVirksomhetMedGradering -> UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(it.årstall, it.kvartal),
                    it.tapteDagsverkGradertSykemelding,
                    it.tapteDagsverk,
                    it.antallPersoner
                )
            }
        }
}

fun List<SykefraværsstatistikkLand>.groupByLand():
        Map<String, List<Sykefraværsstatistikk>> =
    this.groupBy({ "NO" }, { it })

fun List<SykefraværsstatistikkSektor>.groupBySektor():
        Map<String, List<Sykefraværsstatistikk>> =
    this.groupBy({ it.sektorkode }, { it })

fun List<SykefraværsstatistikkForNæring>.groupByNæring():
        Map<String, List<Sykefraværsstatistikk>> =
    this.groupBy({ it.næringkode }, { it })

fun List<SykefraværsstatistikkForNæringskode>.groupByNæringskode():
        Map<String, List<Sykefraværsstatistikk>> =
    this.groupBy({ it.næringkode5siffer }, { it })

fun List<SykefraværsstatistikkBransje>.groupByBransje():
        Map<String, List<Sykefraværsstatistikk>> =
    this.groupBy({ it.bransje.name }, { it })

fun List<SykefraværsstatistikkVirksomhetUtenVarighet>.groupByOrgnr():
        Map<String, List<Sykefraværsstatistikk>> =
    this.groupBy({ it.orgnr }, { it })

fun List<SykefraværsstatistikkVirksomhetMedGradering>.groupByVirksomhet():
        Map<String, List<Sykefraværsstatistikk>> =
    this.groupBy({ it.orgnr }, { it })

fun List<UmaskertSykefraværForEttKvartal>.tilSykefraværMedKategoriSisteKvartal(
    statistikkategori: Statistikkategori,
    kode: String
): SykefraværMedKategori =
    this.maxWith(compareBy({ it.Årstall }, { it.kvartal }))
        .tilSykefraværMedKategori(statistikkategori, kode)


