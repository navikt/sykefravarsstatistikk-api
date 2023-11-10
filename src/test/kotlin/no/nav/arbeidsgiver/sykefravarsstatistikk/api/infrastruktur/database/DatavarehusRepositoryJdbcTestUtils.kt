package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusLandRespository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.jetbrains.exposed.sql.deleteAll
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

object DatavarehusRepositoryJdbcTestUtils {

    fun cleanUpTestDb(jdbcTemplate: NamedParameterJdbcTemplate, datavarehusLandRespository: DatavarehusLandRespository? = null) {
        datavarehusLandRespository?.slettAlt()
        delete(jdbcTemplate, "dt_p.agg_ia_sykefravar_v_2")
        delete(jdbcTemplate, "dt_p.agg_ia_sykefravar_v")
    }

    private fun DatavarehusLandRespository.slettAlt() {
        transaction {
            deleteAll()
        }
    }

    fun delete(jdbcTemplate: NamedParameterJdbcTemplate, tabell: String?): Int {
        return jdbcTemplate.update("delete from $tabell", MapSqlParameterSource())
    }


    fun insertOrgenhetInDvhTabell(
        jdbcTemplate: NamedParameterJdbcTemplate,
        orgnr: String?,
        sektor: String?,
        næring: String?,
        primærnæringskode: String?,
        årstall: Int,
        kvartal: Int
    ) {
        val naringParams = MapSqlParameterSource()
            .addValue("orgnr", orgnr)
            .addValue("sektor", sektor)
            .addValue("naring", næring)
            .addValue("primærnæringskode", primærnæringskode)
            .addValue("årstall", årstall)
            .addValue("kvartal", kvartal)
        jdbcTemplate.update(
            "insert into dt_p.agg_ia_sykefravar_v_2 (orgnr, rectype, sektor, naring, primærnæringskode, arstall, kvartal) "
                    + "values (:orgnr, '2', :sektor, :naring, :primærnæringskode, :årstall, :kvartal)",
            naringParams
        )
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
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET
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

    fun insertSykefraværsstatistikkVirksomhetGraderingInDvhTabell(
        jdbcTemplate: NamedParameterJdbcTemplate,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        orgnr: String?,
        næring: String?,
        næringskode5siffer: String?,
        alder: String?,
        kjonn: String?,
        fylkbo: String?,
        kommnr: String?,
        taptedagsverkGradertSykemelding: Long,
        antallGradertSykemeldinger: Int,
        antallSykemeldinger: Int,
        taptedagsverk: Long,
        muligedagsverk: Long,
        rectype: String?
    ) {
        val params = MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("orgnr", orgnr)
            .addValue("naring", næring)
            .addValue("naering_kode", næringskode5siffer)
            .addValue("alder", alder)
            .addValue("kjonn", kjonn)
            .addValue("fylkbo", fylkbo)
            .addValue("kommnr", kommnr)
            .addValue("rectype", rectype)
            .addValue("antall_gs", antallGradertSykemeldinger)
            .addValue("taptedv_gs", taptedagsverkGradertSykemelding)
            .addValue("antall", antallSykemeldinger)
            .addValue("taptedv", taptedagsverk)
            .addValue("mulige_dv", muligedagsverk)
            .addValue("antpers", antallPersoner)
        jdbcTemplate.update(
            "insert into dt_p.agg_ia_sykefravar_v_2 ("
                    + "arstall, kvartal, "
                    + "orgnr, naring, naering_kode, "
                    + "alder, kjonn,  fylkbo, "
                    + "kommnr, rectype, "
                    + "antall_gs, taptedv_gs, "
                    + "antall, "
                    + "taptedv, mulige_dv, antpers) "
                    + "values ("
                    + ":arstall, :kvartal, "
                    + ":orgnr,:naring, :naering_kode, :alder, "
                    + ":kjonn, :fylkbo, :kommnr, "
                    + ":rectype, "
                    + ":antall_gs, :taptedv_gs, "
                    + ":antall, "
                    + ":taptedv, :mulige_dv, :antpers)",
            params
        )
    }


    fun insertSykefraværsstatistikkVirksomhetGraderingInDvhTabell(
        namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        orgnrVirksomhet1: String?,
        næringskode2siffer: String?,
        næringskode5siffer: String?,
        tapteDagsverkGradertSykemelding: Long,
        antallGradertSykemeldinger: Int,
        antallSykemeldinger: Int,
        tapteDagsverk: Long,
        muligeDagsverk: Long
    ) {
        insertSykefraværsstatistikkVirksomhetGraderingInDvhTabell(
            namedParameterJdbcTemplate,
            årstall,
            kvartal,
            antallPersoner,
            orgnrVirksomhet1,
            næringskode2siffer,
            næringskode5siffer,
            "A",
            "M",
            "03",
            "4200",
            tapteDagsverkGradertSykemelding,
            antallGradertSykemeldinger,
            antallSykemeldinger,
            tapteDagsverk,
            muligeDagsverk,
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET
        )
    }
}
