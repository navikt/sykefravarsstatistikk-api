package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.SykefraværFlereKvartalerForEksport
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
        statistikkategori: Statistikkategori,
    ) {
        log.info(
            "Starter eksportering av kategori '{}' for {} på topic '{}'.",
            statistikkategori.name,
            årstallOgKvartal,
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
            .groupBy({ "NO" }, { it }).let {
                eksporterSykefraværsstatistikkPerKategori(
                    eksportkvartal = årstallOgKvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.LAND,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_LAND_V1
                )
            }
    }

    private fun eksporterSykefraværsstatistikkSektor(årstallOgKvartal: ÅrstallOgKvartal) {
        sykefraværStatistikkSektorRepository.hentForKvartaler(årstallOgKvartal inkludertTidligere 3)
            .groupBy({ it.sektor.name }, { it })
            .let {
                eksporterSykefraværsstatistikkPerKategori(
                    eksportkvartal = årstallOgKvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.SEKTOR,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_SEKTOR_V1
                )
            }
    }

    private fun eksporterSykefraværsstatistikkNæring(årstallOgKvartal: ÅrstallOgKvartal) {
        sykefraværStatistikkNæringRepository.hentForAlleNæringer(
            årstallOgKvartal inkludertTidligere 3
        ).groupBy({ it.næring }, { it }).let {
            eksporterSykefraværsstatistikkPerKategori(
                eksportkvartal = årstallOgKvartal,
                sykefraværGruppertEtterKode = it,
                statistikkategori = Statistikkategori.NÆRING,
                kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_NARING_V1
            )
        }
    }

    private fun eksporterSykefraværsstatistikkNæringskode(årstallOgKvartal: ÅrstallOgKvartal) {
        sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(årstallOgKvartal inkludertTidligere 3)
            .groupBy({ it.næringskode }, { it }).let {
                eksporterSykefraværsstatistikkPerKategori(
                    eksportkvartal = årstallOgKvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.NÆRINGSKODE,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_NARINGSKODE_V1
                )
            }
    }

    private fun eksporterSykefraværsstatistikkBransje(kvartal: ÅrstallOgKvartal) {

        hentSykefraværsstatistikkForBransje(
            kvartaler = kvartal inkludertTidligere 3,
            sykefraværsstatistikkNæringRepository = sykefraværStatistikkNæringRepository,
            sykefraværStatistikkNæringskodeRepository = sykefraværStatistikkNæringskodeRepository
        )
            .groupBy({ it.bransje.name }, { it }).let {
                eksporterSykefraværsstatistikkPerKategori(
                    eksportkvartal = kvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.BRANSJE,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_BRANSJE_V1
                )
            }
    }

    private fun eksporterSykefraværsstatistikkVirksomhet(årstallOgKvartal: ÅrstallOgKvartal) {
        val statistikk =
            sykefravarStatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(årstallOgKvartal inkludertTidligere 3)
                .groupBy({ it.orgnr }, { it })

        eksporterSykefraværsstatistikkPerKategori(
            eksportkvartal = årstallOgKvartal,
            sykefraværGruppertEtterKode = statistikk,
            statistikkategori = Statistikkategori.VIRKSOMHET,
            kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1
        )
    }

    private fun eksporterSykefraværsstatistikkVirksomhetGradert(årstallOgKvartal: ÅrstallOgKvartal) {
        sykefravarStatistikkVirksomhetGraderingRepository.hentSykefraværAlleVirksomheterGradert(
            årstallOgKvartal inkludertTidligere 3
        ).groupBy({ it.orgnr }, { it }).let {
            eksporterSykefraværsstatistikkPerKategori(
                eksportkvartal = årstallOgKvartal,
                sykefraværGruppertEtterKode = it,
                statistikkategori = Statistikkategori.VIRKSOMHET_GRADERT,
                kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_VIRKSOMHET_GRADERT_V1
            )
        }
    }


    private fun eksporterSykefraværsstatistikkPerKategori(
        eksportkvartal: ÅrstallOgKvartal,
        sykefraværGruppertEtterKode: Map<String, List<Sykefraværsstatistikk>>,
        statistikkategori: Statistikkategori,
        kafkaTopic: KafkaTopic,
    ) {
        log.info("Starter utsending av alle meldinger til Kafka for statistikkategori ${statistikkategori.name}")
        var antallStatistikkEksportert = 0
        sykefraværGruppertEtterKode.forEach { (kode, statistikk) ->
            val umaskertSykefraværsstatistikk =
                statistikk.tilUmaskertSykefraværForEttKvartal()

            val sykefraværMedKategoriSisteKvartal =
                umaskertSykefraværsstatistikk
                    .max().tilSykefraværMedKategori(statistikkategori, kode)

            if (eksportkvartal != sykefraværMedKategoriSisteKvartal.årstallOgKvartal) {
                return@forEach
            }

            val melding = when (statistikkategori) {
                Statistikkategori.LAND,
                Statistikkategori.SEKTOR,
                Statistikkategori.NÆRING,
                Statistikkategori.BRANSJE,
                Statistikkategori.OVERORDNET_ENHET,
                Statistikkategori.VIRKSOMHET,
                Statistikkategori.NÆRINGSKODE,
                -> StatistikkategoriKafkamelding(
                    sykefraværMedKategoriSisteKvartal,
                    SykefraværFlereKvartalerForEksport(umaskertSykefraværsstatistikk)
                )

                Statistikkategori.VIRKSOMHET_GRADERT -> GradertStatistikkategoriKafkamelding(
                    sykefraværMedKategoriSisteKvartal,
                    SykefraværFlereKvartalerForEksport(umaskertSykefraværsstatistikk)
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
                is SykefraværsstatistikkVirksomhet,
                -> UmaskertSykefraværForEttKvartal(it)

                is SykefraværsstatistikkVirksomhetMedGradering -> UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(it.årstall, it.kvartal),
                    it.tapteDagsverkGradertSykemelding,
                    it.tapteDagsverk,
                    it.antallPersoner
                )
            }
        }
}


