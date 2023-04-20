package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.SykefraværsstatistikkBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.math.BigDecimal
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

operator fun SykefraværsstatistikkNæring.plus(other: SykefraværsstatistikkNæring): SykefraværsstatistikkNæring {
    return SykefraværsstatistikkNæring(
        this.Årstall,
        this.kvartal,
        this.næringkode,
        this.antallPersoner + other.antallPersoner,
        this.tapteDagsverk!! + other.tapteDagsverk!!,
        this.muligeDagsverk!! + other.muligeDagsverk!!,
    )
}

private fun hentSykefraværsstatistikkForAngitteNæringskoder(
    namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    kvartal: ÅrstallOgKvartal,
    næringskoder: List<String>
): List<SykefraværsstatistikkNæring> {
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
): List<SykefraværsstatistikkNæring> =
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
    val bransjer = Bransjeprogram.bransjer.map { it.koderSomSpesifisererNæringer }.flatten()
    val næringer = bransjer.filter { it.length == 2 }
    val næringskoder = bransjer.filter { it.length == 5 }
    return Pair(næringer, næringskoder)
}

private fun getKvartalsvisSykefraværsstatistikkNæringer(
    namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    kvartal: ÅrstallOgKvartal,
    næringer: List<String>,
    næringskoder: List<String>
): Map<ÅrstallOgKvartal, List<SykefraværsstatistikkNæring>> {

    val statistikkNæringer = hentSykefraværsstatistikkForAngitteNæringer(
        namedParameterJdbcTemplate, kvartal, næringer
    ) + hentSykefraværsstatistikkForAngitteNæringskoder(
        namedParameterJdbcTemplate, kvartal, næringskoder
    )

    return statistikkNæringer.groupBy { ÅrstallOgKvartal(it.Årstall, it.kvartal) }
}

private fun summerSykefraværsstatistikkPerBransje(
    kvartalsvisSykefraværsstatistikkNæringer: Map<ÅrstallOgKvartal, List<SykefraværsstatistikkNæring>>
): List<SykefraværsstatistikkBransje> {
    return kvartalsvisSykefraværsstatistikkNæringer.flatMap { (_, sykefraværsstatistikkNæringer) ->
        Bransjeprogram.bransjer.mapNotNull { bransje ->
            val filtrertPåBransje = hentStatistikkForNæringerSomTilhørerBransje(sykefraværsstatistikkNæringer, bransje)
            summerSykefraværsstatistikkNæringForEttKvartal(filtrertPåBransje, bransje)
        }
    }
}

private fun hentStatistikkForNæringerSomTilhørerBransje(
    sykefraværsstatistikkNæringer: List<SykefraværsstatistikkNæring>,
    bransje: Bransje
): List<SykefraværsstatistikkNæring> {
    val koderSomSpesifisererNæringer = bransje.koderSomSpesifisererNæringer
    return sykefraværsstatistikkNæringer.filter { sykefraværsstatistikkNæring ->
        koderSomSpesifisererNæringer.contains(sykefraværsstatistikkNæring.næringkode)
    }
}

private fun summerSykefraværsstatistikkNæringForEttKvartal(
    filteredNæringer: List<SykefraværsstatistikkNæring>,
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
            tapteDagsverk = BigDecimal.ZERO,
            muligeDagsverk = BigDecimal.ZERO,
        )
    ) { akkumulertStatistikkBransje, sykefraværsstatistikkNæring ->
        akkumulertStatistikkBransje.copy(
            årstall = sykefraværsstatistikkNæring.Årstall,
            kvartal = sykefraværsstatistikkNæring.kvartal,
            antallPersoner = akkumulertStatistikkBransje.antallPersoner + sykefraværsstatistikkNæring.antallPersoner,
            tapteDagsverk = akkumulertStatistikkBransje.tapteDagsverk + sykefraværsstatistikkNæring.tapteDagsverk!!,
            muligeDagsverk = akkumulertStatistikkBransje.muligeDagsverk + sykefraværsstatistikkNæring.muligeDagsverk!!,
        )
    }
}

private fun allStatistikkErFraSammeKvartal(filteredNæringer: List<SykefraværsstatistikkNæring>) =
    filteredNæringer.all {
        it.Årstall == filteredNæringer.first().Årstall && it.kvartal == filteredNæringer.first().kvartal
    }
