package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import ia.felles.definisjoner.bransjer.BransjeId
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import java.math.BigDecimal.ZERO

fun hentSykefraværsstatistikkForBransje(
    kvartaler: List<ÅrstallOgKvartal>,
    sykefraværsstatistikkNæringRepository: SykefraværStatistikkNæringRepository,
    sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository
): List<SykefraværsstatistikkBransje> {
    val statistikkForNæring = sykefraværsstatistikkNæringRepository.hentForAlleNæringer(kvartaler)
        .groupBy { ÅrstallOgKvartal(it.årstall, it.kvartal) }

    val statistikkForNæringskoder = sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(kvartaler)
        .groupBy { ÅrstallOgKvartal(it.årstall, it.kvartal) }

    return statistikkForNæring
        .flatMap { summerSykefraværsstatistikkPerBransjeNæringer(it.value) } +
            statistikkForNæringskoder.flatMap {
                summerSykefraværsstatistikkPerBransjeNæringskoder(
                    it.value
                )
            }
}


fun summerSykefraværsstatistikkPerBransjeNæringer(
    statistikk: List<SykefraværsstatistikkForNæring>
): List<SykefraværsstatistikkBransje> {

    val bransjer = Bransjeprogram.alleBransje
        .map { bransje ->
            bransje to statistikk.filter {
                bransje.bransjeId.let { bransjeId ->
                    when (bransjeId) {
                        is BransjeId.Næring -> bransjeId.næring == it.næringkode
                        is BransjeId.Næringskoder -> false
                    }
                }
            }
        }.filter { it.second.isNotEmpty() }

    val sumPerBransje: MutableList<SykefraværsstatistikkBransje> = mutableListOf()

    for ((bransje, bransjedata) in bransjer) {

        val (årstall, kvartal) = bransjedata.first()

        sumPerBransje +=
            SykefraværsstatistikkBransje(
                bransje = bransje,
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

    val bransjer = Bransjeprogram.alleBransje
        .map { bransje ->
            bransje to statistikk.filter {
                bransje.bransjeId.let { bransjeId ->
                    when (bransjeId) {
                        is BransjeId.Næring -> false
                        is BransjeId.Næringskoder -> bransjeId.næringskoder.contains(it.næringkode5siffer)
                    }
                }

            }
        }.filter { it.second.isNotEmpty() }

    val sumPerBransje: MutableList<SykefraværsstatistikkBransje> = mutableListOf()

    for ((bransje, bransjedata) in bransjer) {

        val (årstall, kvartal) = bransjedata.first()

        sumPerBransje +=
            SykefraværsstatistikkBransje(
                bransje = bransje,
                årstall = årstall,
                kvartal = kvartal,
                antallPersoner = bransjedata.sumOf { it.antallPersoner },
                tapteDagsverk = bransjedata.sumOf { it.tapteDagsverk },
                muligeDagsverk = bransjedata.sumOf { it.muligeDagsverk },
            )
    }

    return sumPerBransje
}