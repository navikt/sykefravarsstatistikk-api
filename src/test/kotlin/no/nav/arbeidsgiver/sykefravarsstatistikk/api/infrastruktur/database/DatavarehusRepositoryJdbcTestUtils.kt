package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusAggregertRepositoryV2
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusLandRespository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.Rectype
import org.jetbrains.exposed.sql.deleteAll
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

object DatavarehusRepositoryJdbcTestUtils {

    fun cleanUpTestDb(
        jdbcTemplate: NamedParameterJdbcTemplate,
        datavarehusLandRespository: DatavarehusLandRespository,
        datavarehusAggregertRepositoryV2: DatavarehusAggregertRepositoryV2
    ) {
        datavarehusLandRespository.slettAlt()
        datavarehusAggregertRepositoryV2.slettAlt()
        delete(jdbcTemplate, "dt_p.agg_ia_sykefravar_v")
    }

    private fun DatavarehusAggregertRepositoryV2.slettAlt() {
        transaction {
            deleteAll()
        }
    }

    private fun DatavarehusLandRespository.slettAlt() {
        transaction {
            deleteAll()
        }
    }


    fun delete(jdbcTemplate: NamedParameterJdbcTemplate, tabell: String?): Int {
        return jdbcTemplate.update("delete from $tabell", MapSqlParameterSource())
    }

    fun insertSykefraværsstatistikkVirksomhetInDvhTabell(
        jdbcTemplate: NamedParameterJdbcTemplate,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        orgnr: String?,
        næringskode5siffer: String?,
        varighet: Varighetskategori,
        kjonn: String?,
        taptedagsverk: Long,
        muligedagsverk: Long
    ) {
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate,
            årstall,
            kvartal,
            antallPersoner,
            orgnr,
            næringskode5siffer,
            varighet,
            kjonn,
            taptedagsverk,
            muligedagsverk,
            Rectype.VIRKSOMHET.kode
        )
    }


    fun insertSykefraværsstatistikkVirksomhetInDvhTabell(
        jdbcTemplate: NamedParameterJdbcTemplate,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        orgnr: String?,
        næringskode: String?,
        varighet: Varighetskategori,
        kjonn: String?,
        taptedagsverk: Long,
        muligedagsverk: Long,
        rectype: String?
    ) {
        val params = MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("antpers", antallPersoner)
            .addValue("orgnr", orgnr)
            .addValue("varighet", varighet.kode)
            .addValue("naering_kode", næringskode)
            .addValue("sektor", Sektor.PRIVAT.sektorkode)
            .addValue("kjonn", kjonn)
            .addValue("taptedv", taptedagsverk)
            .addValue("muligedv", muligedagsverk)
            .addValue("rectype", rectype)
        jdbcTemplate.update(
            "insert into dt_p.agg_ia_sykefravar_v ("
                    + "arstall, kvartal, "
                    + "orgnr, naering_kode, sektor, storrelse, fylkarb, "
                    + "alder, kjonn,  fylkbo, "
                    + "sftype, varighet, "
                    + "taptedv, muligedv, antpers, rectype) "
                    + "values ("
                    + ":arstall, :kvartal, "
                    + ":orgnr, :naering_kode, :sektor, 'G', '03', "
                    + "'B', :kjonn, '02', "
                    + "'L', :varighet, "
                    + ":taptedv, :muligedv, :antpers, :rectype)",
            params
        )
    }

    fun insertSykefraværsstatistikkNæringInDvhTabell(
        jdbcTemplate: NamedParameterJdbcTemplate,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        næring: String?,
        kjonn: String?,
        taptedagsverk: Long,
        muligedagsverk: Long
    ) {
        val params = MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("antpers", antallPersoner)
            .addValue("naring", næring)
            .addValue("kjonn", kjonn)
            .addValue("taptedv", taptedagsverk)
            .addValue("muligedv", muligedagsverk)
        jdbcTemplate.update(
            "insert into dt_p.v_agg_ia_sykefravar_naring ("
                    + "arstall, kvartal, "
                    + "naring, "
                    + "alder, kjonn, "
                    + "taptedv, muligedv, antpers) "
                    + "values ("
                    + ":arstall, :kvartal, "
                    + ":naring, "
                    + "'A', :kjonn, "
                    + ":taptedv, :muligedv, :antpers)",
            params
        )
    }


    fun insertSykefraværsstatistikkNærin5SiffergInDvhTabell(
        jdbcTemplate: NamedParameterJdbcTemplate,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        næringKode: String?,
        kjonn: String?,
        taptedagsverk: Long,
        muligedagsverk: Long
    ) {
        val params = MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("antpers", antallPersoner)
            .addValue("næringKode", næringKode)
            .addValue("kjonn", kjonn)
            .addValue("taptedv", taptedagsverk)
            .addValue("muligedv", muligedagsverk)
        jdbcTemplate.update(
            "insert into dt_p.agg_ia_sykefravar_naring_kode ("
                    + "arstall, kvartal, "
                    + "naering_kode, "
                    + "alder, kjonn, "
                    + "taptedv, muligedv, antpers) "
                    + "values ("
                    + ":arstall, :kvartal, "
                    + ":næringKode, "
                    + "'A', :kjonn, "
                    + ":taptedv, :muligedv, :antpers)",
            params
        )
    }
}
