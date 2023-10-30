package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.math.BigDecimal.ZERO
import java.sql.ResultSet

fun hentSykefraværsstatistikkForBransjer(
    kvartaler: List<ÅrstallOgKvartal>,
    namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    sykefraværsstatistikkNæringRepository: SykefraværStatistikkNæringRepository,
): List<SykefraværsstatistikkBransje> {
    val (næringer, næringskoder) = getNæringerAndNæringskoderSomUtgjørBransjene()

    return try {
        val statistikkForNæring = sykefraværsstatistikkNæringRepository.hentForAngitteNæringer(
            kvartaler,
            næringer
        )

        val statistikkForNæringskoder = kvartaler.associateWith { kvartal ->
            hentSykefraværsstatistikkForAngitteNæringskoder(
                namedParameterJdbcTemplate, kvartal, næringskoder.map { it.femsifferIdentifikator }
            )
        }
        summerSykefraværsstatistikkPerBransje(
            statistikkForNæringskoder + statistikkForNæring
        )
    } catch (e: EmptyResultDataAccessException) {
        emptyList()
    }
}

operator fun SykefraværsstatistikkForNæring.plus(other: SykefraværsstatistikkForNæring): SykefraværsstatistikkForNæring {
    return SykefraværsstatistikkForNæring(
        this.årstall,
        this.kvartal,
        this.næringkode,
        this.antallPersoner + other.antallPersoner,
        (this.tapteDagsverk ?: ZERO) + (other.tapteDagsverk ?: ZERO),
        (this.muligeDagsverk ?: ZERO) + (other.muligeDagsverk ?: ZERO),
    )
}

private fun hentSykefraværsstatistikkForAngitteNæringskoder(
    namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    kvartal: ÅrstallOgKvartal,
    næringskoder: List<String>
): List<SykefraværsstatistikkForNæring> {
    return namedParameterJdbcTemplate.query(
        """
            select arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, mulige_dagsverk
            from sykefravar_statistikk_naring5siffer
            where (arstall = :arstall and kvartal = :kvartal)
            and naring_kode in (:naringskoder)
        """.trimMargin(),
        MapSqlParameterSource()
            .addValue("arstall", kvartal.årstall)
            .addValue("kvartal", kvartal.kvartal)
            .addValue("naringskoder", næringskoder),
        sykefraværsstatistikkNæringRowMapper(),
    )
}

private fun sykefraværsstatistikkNæringRowMapper() = RowMapper { resultSet: ResultSet, _: Int ->
    SykefraværsstatistikkTilEksporteringRepository.mapTilSykefraværsstatistikkNæring(resultSet)
}

private fun getNæringerAndNæringskoderSomUtgjørBransjene(): Pair<List<Næring>, List<Næringskode>> {
    val bransjer = Bransjeprogram.alleBransjer.map { it.identifikatorer }.flatten()
    val næringer = bransjer.filter { it.length == 2 }.map { Næring(it) }
    val næringskoder = bransjer.filter { it.length == 5 }.map { Næringskode(it) }
    return Pair(næringer, næringskoder)
}

fun summerSykefraværsstatistikkPerBransje(
    kvartalsvisSykefraværsstatistikkForNæringer: Map<ÅrstallOgKvartal, List<SykefraværsstatistikkForNæring>>
): List<SykefraværsstatistikkBransje> {
    return kvartalsvisSykefraværsstatistikkForNæringer.flatMap { (_, sykefraværsstatistikkNæringer) ->
        Bransjeprogram.alleBransjer.mapNotNull { bransje ->
            val filtrertPåBransje = hentStatistikkForNæringerSomTilhørerBransje(sykefraværsstatistikkNæringer, bransje)
            summerSykefraværsstatistikkNæringForEttKvartal(filtrertPåBransje, bransje)
        }
    }
}

private fun hentStatistikkForNæringerSomTilhørerBransje(
    sykefraværsstatistikkForNæringer: List<SykefraværsstatistikkForNæring>,
    bransje: Bransje
): List<SykefraværsstatistikkForNæring> {
    val koderSomSpesifisererNæringer = bransje.identifikatorer
    return sykefraværsstatistikkForNæringer.filter { sykefraværsstatistikkNæring ->
        koderSomSpesifisererNæringer.contains(sykefraværsstatistikkNæring.næringkode)
    }
}

private fun summerSykefraværsstatistikkNæringForEttKvartal(
    filteredNæringer: List<SykefraværsstatistikkForNæring>,
    bransje: Bransje,
): SykefraværsstatistikkBransje? {
    if (filteredNæringer.isEmpty()) {
        return null
    }

    if (!allStatistikkErFraSammeKvartal(filteredNæringer)
    ) {
        throw IllegalStateException("Kan ikke summere sykefraværsstatistikk for flere kvartaler")
    }

    return filteredNæringer.fold(
        SykefraværsstatistikkBransje(
            årstall = 0,
            kvartal = 0,
            bransje = bransje.type,
            antallPersoner = 0,
            tapteDagsverk = ZERO,
            muligeDagsverk = ZERO,
        )
    ) { akkumulertStatistikkBransje, sykefraværsstatistikkNæring ->
        akkumulertStatistikkBransje.copy(
            årstall = sykefraværsstatistikkNæring.årstall,
            kvartal = sykefraværsstatistikkNæring.kvartal,
            antallPersoner = akkumulertStatistikkBransje.antallPersoner + sykefraværsstatistikkNæring.antallPersoner,
            tapteDagsverk = akkumulertStatistikkBransje.tapteDagsverk + (sykefraværsstatistikkNæring.tapteDagsverk
                ?: ZERO),
            muligeDagsverk = akkumulertStatistikkBransje.muligeDagsverk + (sykefraværsstatistikkNæring.muligeDagsverk
                ?: ZERO),
        )
    }
}

private fun allStatistikkErFraSammeKvartal(filteredNæringer: List<SykefraværsstatistikkForNæring>) =
    filteredNæringer.all {
        it.årstall == filteredNæringer.first().årstall && it.kvartal == filteredNæringer.first().kvartal
    }
