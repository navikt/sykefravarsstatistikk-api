package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.UmaskertSykefraværForEttKvartalMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

@Component
class SykefraværStatistikkNæringMedVarighetRepository(override val database: Database) : UsingExposed,
    Table("sykefravar_statistikk_naring_med_varighet") {

    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val næring = varchar("naring_kode", length = 2)
    val tapteDagsverk = double("tapte_dagsverk")
    val muligeDagsverk = double("mulige_dagsverk")
    val antallPersoner = integer("antall_personer")
    val varighet = char("varighet")

    fun hentSykefraværMedVarighetNæring(
        næringa: Næring
    ): List<UmaskertSykefraværForEttKvartalMedVarighet> {
        return transaction {
            select { næring eq næringa.tosifferIdentifikator }
                .map {
                    UmaskertSykefraværForEttKvartalMedVarighet(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                        muligeDagsverk = it[muligeDagsverk].toBigDecimal(),
                        antallPersoner = it[antallPersoner],
                        varighet = Varighetskategori.fraKode(it[varighet].toString())
                    )
                }
        }
    }

    fun hentSykefraværMedVarighetBransje(
        bransje: Bransje
    ): List<UmaskertSykefraværForEttKvartalMedVarighet> {
        return try {
            namedParameterJdbcTemplate.query(
                "select tapte_dagsverk, mulige_dagsverk, antall_personer, varighet, arstall, kvartal "
                        + " from sykefravar_statistikk_naring_med_varighet "
                        + " where "
                        + " naring_kode in (:naringKoder) "
                        + " and varighet in ('A', 'B', 'C', 'D', 'E', 'F', 'X')"
                        + " order by arstall, kvartal, varighet",
                MapSqlParameterSource()
                    .addValue("naringKoder", bransje.identifikatorer)
            ) { rs: ResultSet, _: Int -> mapTilKvartalsvisSykefraværMedVarighet(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }


    @Throws(SQLException::class)
    private fun mapTilKvartalsvisSykefraværMedVarighet(
        rs: ResultSet
    ): UmaskertSykefraværForEttKvartalMedVarighet {
        return UmaskertSykefraværForEttKvartalMedVarighet(
            ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
            rs.getBigDecimal("tapte_dagsverk"),
            rs.getBigDecimal("mulige_dagsverk"),
            rs.getInt("antall_personer"),
            Varighetskategori.fraKode(rs.getString("varighet"))
        )
    }
}
