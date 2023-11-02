package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import java.math.BigDecimal.ZERO

fun hentSykefraværsstatistikkForBransjer(
    kvartaler: List<ÅrstallOgKvartal>,
    sykefraværsstatistikkNæringRepository: SykefraværStatistikkNæringRepository,
    sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository
): List<SykefraværsstatistikkBransje> {
    val statistikkForNæring = sykefraværsstatistikkNæringRepository.hentForAlleNæringer(kvartaler)

    val statistikkForNæringskoder = sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(kvartaler)

    val summert = mutableListOf<SykefraværsstatistikkBransje>()

    for (kvarttal in kvartaler) {
        summert += summerSykefraværsstatistikkPerBransjeNæringskoder(statistikkForNæringskoder.filter {
            ÅrstallOgKvartal(
                it.årstall,
                it.kvartal
            ) == kvarttal
        }) +
                summerSykefraværsstatistikkPerBransjeNæringer(statistikkForNæring.filter {
                    ÅrstallOgKvartal(
                        it.årstall,
                        it.kvartal
                    ) == kvarttal
                })
    }

    return summert
}


fun summerSykefraværsstatistikkPerBransjeNæringer(
    statistikk: List<SykefraværsstatistikkForNæring>
): List<SykefraværsstatistikkBransje> {

    val bransjer = Bransjeprogram.alleBransjer
        .map { bransje ->
            bransje to statistikk.filter {
                bransje.identifikatorer.contains(it.næringkode)
            }
        }.filter { it.second.isNotEmpty() }

    val sumPerBransje: MutableList<SykefraværsstatistikkBransje> = mutableListOf()

    for ((bransje, bransjedata) in bransjer) {

        val (årstall, kvartal) = bransjedata.first()

        sumPerBransje +=
            SykefraværsstatistikkBransje(
                bransje = bransje.type,
                årstall = årstall,
                kvartal = kvartal,
                antallPersoner = bransjedata.sumOf { it.antallPersoner },
                tapteDagsverk = bransjedata.sumOf { it.tapteDagsverk ?: ZERO },
                muligeDagsverk = bransjedata.sumOf { it.muligeDagsverk ?: ZERO },
            )
    }

    return sumPerBransje
}


fun summerSykefraværsstatistikkPerBransjeNæringskoder(
    statistikk: List<SykefraværsstatistikkForNæringskode>
): List<SykefraværsstatistikkBransje> {

    val bransjer = Bransjeprogram.alleBransjer
        .map { bransje ->
            bransje to statistikk.filter {
                bransje.identifikatorer.contains(it.næringkode5siffer)
            }
        }.filter { it.second.isNotEmpty() }

    val sumPerBransje: MutableList<SykefraværsstatistikkBransje> = mutableListOf()

    for ((bransje, bransjedata) in bransjer) {

        val (årstall, kvartal) = bransjedata.first()

        sumPerBransje +=
            SykefraværsstatistikkBransje(
                bransje = bransje.type,
                årstall = årstall,
                kvartal = kvartal,
                antallPersoner = bransjedata.sumOf { it.antallPersoner },
                tapteDagsverk = bransjedata.sumOf { it.tapteDagsverk },
                muligeDagsverk = bransjedata.sumOf { it.muligeDagsverk },
            )
    }

    return sumPerBransje
}