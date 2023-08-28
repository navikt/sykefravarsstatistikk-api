package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværsstatistikkForNæring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransjeprogram
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.math.BigDecimal.ZERO
import java.sql.ResultSet

fun hentSykefraværsstatistikkForBransjerFraOgMed(
    kvartal: ÅrstallOgKvartal,
    namedParameterJdbcTemplate: NamedParameterJdbcTemplate
): List<SykefraværsstatistikkBransje> {
    val (næringer, næringskoder) = getNæringerAndNæringskoderSomUtgjørBransjene()

    return try {
        val kvartalsvisSykefraværsstatistikkNæringer = getKvartalsvisSykefraværsstatistikkNæringer(
            namedParameterJdbcTemplate, kvartal, næringer, næringskoder
        )
        summerSykefraværsstatistikkPerBransje(kvartalsvisSykefraværsstatistikkNæringer)
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
            where (arstall = :arstall and kvartal >= :kvartal) or (arstall > :arstall)
            and naring_kode in (:naringskoder)
        """.trimMargin(),
        MapSqlParameterSource()
            .addValue("arstall", kvartal.årstall)
            .addValue("kvartal", kvartal.kvartal)
            .addValue("naringskoder", næringskoder),
        sykefraværsstatistikkNæringRowMapper(),
    )
}

private fun hentSykefraværsstatistikkForAngitteNæringer(
    namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    kvartal: ÅrstallOgKvartal,
    næringer: List<String>
): List<SykefraværsstatistikkForNæring> =
    namedParameterJdbcTemplate.query(
        """
            select arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, mulige_dagsverk
            from sykefravar_statistikk_naring
            where (arstall = :arstall and kvartal >= :kvartal) or (arstall > :arstall)
            and naring_kode in (:naringer)
            """.trimMargin(),
        MapSqlParameterSource()
            .addValue("arstall", kvartal.årstall)
            .addValue("kvartal", kvartal.kvartal)
            .addValue("naringer", næringer),
        sykefraværsstatistikkNæringRowMapper(),
    )

private fun sykefraværsstatistikkNæringRowMapper() = RowMapper { resultSet: ResultSet?, _: Int ->
    SykefraværsstatistikkTilEksporteringRepository.mapTilSykefraværsstatistikkNæring(resultSet)
}

private fun getNæringerAndNæringskoderSomUtgjørBransjene(): Pair<List<String>, List<String>> {
    val bransjer = Bransjeprogram.alleBransjer.map { it.identifikatorer }.flatten()
    val næringer = bransjer.filter { it.length == 2 }
    val næringskoder = bransjer.filter { it.length == 5 }
    return Pair(næringer, næringskoder)
}

private fun getKvartalsvisSykefraværsstatistikkNæringer(
    namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    kvartal: ÅrstallOgKvartal,
    næringer: List<String>,
    næringskoder: List<String>
): Map<ÅrstallOgKvartal, List<SykefraværsstatistikkForNæring>> {

    val statistikkNæringer = hentSykefraværsstatistikkForAngitteNæringer(
        namedParameterJdbcTemplate, kvartal, næringer
    ) + hentSykefraværsstatistikkForAngitteNæringskoder(
        namedParameterJdbcTemplate, kvartal, næringskoder
    )

    return statistikkNæringer.groupBy { ÅrstallOgKvartal(it.årstall, it.kvartal) }
}

private fun summerSykefraværsstatistikkPerBransje(
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
