package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværsstatistikkTilEksporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic.Companion.from
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.StatistikkategoriKafkamelding
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EksporteringPerStatistikkKategoriService(
    private val sykefraværRepository: SykefraværRepository,
    private val tilEksporteringRepository: SykefraværsstatistikkTilEksporteringRepository,
    private val kafkaClient: KafkaClient,
    @param:Value("\${statistikk.eksportering.aktivert}") private val erEksporteringAktivert: Boolean
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun eksporterPerStatistikkKategori(
        årstallOgKvartal: ÅrstallOgKvartal,
        statistikkategori: Statistikkategori
    ) {
        if (!erEksporteringAktivert) {
            log.info("Eksportering er ikke aktivert. Avbryter.")
            return
        }
        log.info(
                "Starter eksportering av kategori '{}' for årstall '{}' og kvartal '{}' på topic '{}'.",
                statistikkategori.name,
                årstallOgKvartal.årstall,
                årstallOgKvartal.kvartal,
                from(statistikkategori).navn)

        when (statistikkategori) {
            Statistikkategori.LAND -> eksporterSykefraværsstatistikkLand(årstallOgKvartal)
            Statistikkategori.SEKTOR -> eksporterSykefraværsstatistikkSektor(årstallOgKvartal)
            Statistikkategori.NÆRING -> eksporterSykefraværsstatistikkNæring(årstallOgKvartal)
            Statistikkategori.NÆRINGSKODE -> eksporterSykefraværsstatistikkNæringskode(årstallOgKvartal)
            Statistikkategori.BRANSJE -> eksporterSykefraværsstatistikkBransje(årstallOgKvartal)
            Statistikkategori.VIRKSOMHET -> eksporterSykefraværsstatistikkVirksomhet(årstallOgKvartal)
            else -> log.warn("Ikke implementert eksport for kategori '{}'", statistikkategori.name)
        }

        log.info("Eksportering av kategori '{}' er ferdig.", statistikkategori.name)
    }

    protected fun eksporterSykefraværsstatistikkLand(årstallOgKvartal: ÅrstallOgKvartal) {
        sykefraværRepository.hentUmaskertSykefraværForNorge(
                årstallOgKvartal.minusKvartaler(3)
        ).tilSykefraværsstatistikkLand()
                .groupByLand().let {
                    eksporterSykefraværsstatistikkPerKategori(
                            årstallOgKvartal = årstallOgKvartal,
                            sykefraværGruppertEtterKode = it,
                            statistikkategori = Statistikkategori.LAND,
                            kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_LAND_V1
                    )
                }
    }

    protected fun eksporterSykefraværsstatistikkSektor(årstallOgKvartal: ÅrstallOgKvartal) {
        tilEksporteringRepository.hentSykefraværAlleSektorerFraOgMed(
                årstallOgKvartal.minusKvartaler(3)
        ).groupBySektor().let {
            eksporterSykefraværsstatistikkPerKategori(
                    årstallOgKvartal = årstallOgKvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.NÆRING,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_NARING_V1
            )
        }
    }

    protected fun eksporterSykefraværsstatistikkNæring(årstallOgKvartal: ÅrstallOgKvartal) {
        tilEksporteringRepository.hentSykefraværAlleNæringerFraOgMed(
                årstallOgKvartal.minusKvartaler(3)
        ).groupByNæring().let {
            eksporterSykefraværsstatistikkPerKategori(
                    årstallOgKvartal = årstallOgKvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.NÆRING,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_NARING_V1
            )
        }
    }

    protected fun eksporterSykefraværsstatistikkNæringskode(årstallOgKvartal: ÅrstallOgKvartal) {
        tilEksporteringRepository.hentSykefraværprosentAlleNæringer5Siffer(
                årstallOgKvartal.minusKvartaler(3),
                årstallOgKvartal
        ).groupByNæringskode().let {
            eksporterSykefraværsstatistikkPerKategori(
                    årstallOgKvartal = årstallOgKvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.NÆRINGSKODE,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_NARINGSKODE_V1
            )
        }
    }

    protected fun eksporterSykefraværsstatistikkBransje(årstallOgKvartal: ÅrstallOgKvartal) {
        tilEksporteringRepository.hentSykefraværAlleBransjerFraOgMed(
                årstallOgKvartal.minusKvartaler(3)
        ).groupByBransje().let {
            eksporterSykefraværsstatistikkPerKategori(
                    årstallOgKvartal = årstallOgKvartal,
                    sykefraværGruppertEtterKode = it,
                    statistikkategori = Statistikkategori.BRANSJE,
                    kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_BRANSJE_V1
            )
        }
    }

    protected fun eksporterSykefraværsstatistikkVirksomhet(årstallOgKvartal: ÅrstallOgKvartal) {
        tilEksporteringRepository.hentSykefraværAlleVirksomheter(
                årstallOgKvartal.minusKvartaler(3), årstallOgKvartal)
                .groupByVirksomhet().let {
                    eksporterSykefraværsstatistikkPerKategori(
                            årstallOgKvartal = årstallOgKvartal,
                            sykefraværGruppertEtterKode = it,
                            statistikkategori = Statistikkategori.VIRKSOMHET,
                            kafkaTopic = KafkaTopic.SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1
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
        sykefraværGruppertEtterKode.forEach { (kode: String, statistikk: List<Sykefraværsstatistikk>) ->
            val umaskertSykefraværsstatistikkSiste4Kvartaler: List<UmaskertSykefraværForEttKvartal> =
                    statistikk.tilUmaskertSykefraværForEttKvartal()
            val sykefraværMedKategoriSisteKvartal =
                    umaskertSykefraværsstatistikkSiste4Kvartaler
                            .tilSykefraværMedKategoriSisteKvartal(statistikkategori, kode)

            val erValidertStatistikk = assertForespurteKvartalFinnesIStatistikken(
                    årstallOgKvartal,
                    sykefraværMedKategoriSisteKvartal,
                    statistikkategori,
                    kode
            )

            if (erValidertStatistikk) {
                val melding = StatistikkategoriKafkamelding(
                        sykefraværMedKategoriSisteKvartal,
                        SykefraværFlereKvartalerForEksport(umaskertSykefraværsstatistikkSiste4Kvartaler)
                )
                kafkaClient.sendMelding(melding, kafkaTopic)
                antallStatistikkEksportert++
            }
        }
        log.info("Ferdig med utsending av alle meldinger til Kafka for statistikkategori ${statistikkategori.name}. " +
                "Antall statistikk eksportert: $antallStatistikkEksportert")
    }


    private fun assertForespurteKvartalFinnesIStatistikken(
        årstallOgKvartal: ÅrstallOgKvartal,
        sykefravær: SykefraværMedKategori,
        statistikkategori: Statistikkategori,
        kode: String
    ): Boolean {
        if (årstallOgKvartal != sykefravær.ÅrstallOgKvartal) {
            val message = "Siste kvartal i dataene '${sykefravær.ÅrstallOgKvartal?.årstall}-${sykefravær.ÅrstallOgKvartal?.kvartal}' " +
                    "er ikke lik forespurt kvartal '${årstallOgKvartal.årstall}-${årstallOgKvartal.kvartal}'. " +
                    "Kategori er '${statistikkategori.name}' og kode er '$kode'"

            when (statistikkategori) {
                Statistikkategori.NÆRINGSKODE,
                Statistikkategori.VIRKSOMHET -> {
                    log.info("$message. Ingen kafka melding blir sendt. Fortsetter eksport. ")
                }
                else -> throw RuntimeException("Stopper eksport av kategori ${statistikkategori.name} pga: '$message'")
            }
            return false
        }
        return true
    }
}

fun List<Sykefraværsstatistikk>.tilUmaskertSykefraværForEttKvartal() =
        this.map {
            UmaskertSykefraværForEttKvartal(
                ÅrstallOgKvartal(it.Årstall, it.kvartal),
                it.tapteDagsverk,
                it.muligeDagsverk,
                it.antallPersoner
            )
        }

fun List<UmaskertSykefraværForEttKvartal>.tilSykefraværsstatistikkLand() =
        this.map { it.tilSykefraværsstatistikkLand() }

fun List<SykefraværsstatistikkLand>.groupByLand():
        Map<String, List<Sykefraværsstatistikk>> =
        this.groupBy({ "NO" }, { it })

fun List<SykefraværsstatistikkSektor>.groupBySektor():
        Map<String, List<Sykefraværsstatistikk>> =
        this.groupBy({ it.sektorkode }, { it })

fun List<SykefraværsstatistikkNæring>.groupByNæring():
        Map<String, List<Sykefraværsstatistikk>> =
        this.groupBy({ it.næringkode }, { it })

fun List<SykefraværsstatistikkNæring5Siffer>.groupByNæringskode():
        Map<String, List<Sykefraværsstatistikk>> =
        this.groupBy({ it.næringkode5siffer }, { it })

fun List<SykefraværsstatistikkBransje>.groupByBransje():
        Map<String, List<Sykefraværsstatistikk>> =
        this.groupBy({ it.bransje.name }, { it })

fun List<SykefraværsstatistikkVirksomhetUtenVarighet>.groupByVirksomhet():
        Map<String, List<Sykefraværsstatistikk>> =
        this.groupBy({ it.orgnr }, { it })

fun List<UmaskertSykefraværForEttKvartal>.tilSykefraværMedKategoriSisteKvartal(
    statistikkategori: Statistikkategori,
    kode: String
): SykefraværMedKategori =
        this.maxWith(compareBy({ it.Årstall }, { it.kvartal }))
                .tilSykefraværMedKategori(statistikkategori, kode)

