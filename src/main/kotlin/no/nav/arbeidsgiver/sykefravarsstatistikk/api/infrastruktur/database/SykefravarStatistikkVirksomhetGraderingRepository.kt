package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.springframework.stereotype.Component

@Component
class SykefravarStatistikkVirksomhetGraderingRepository(
    override val database: Database
) : UsingExposed, Table("sykefravar_statistikk_virksomhet_med_gradering") {

    val orgnr = varchar("orgnr", 20)
    val næring = text("naring")
    val næringskode = text("naring_kode")
    val årstall = integer("arstall")
    val kvartal = integer("kvartal")
    val antallGraderteSykemeldinger = integer("antall_graderte_sykemeldinger")
    val tapteDagsverkGradertSykemelding = float("tapte_dagsverk_gradert_sykemelding")
    val antallSykemeldinger = integer("antall_sykemeldinger")
    val antallPersoner = integer("antall_personer")
    val tapteDagsverk = float("tapte_dagsverk")
    val muligeDagsverk = float("mulige_dagsverk")
    val rectype = varchar("rectype", 1)

    fun slettDataEldreEnn(årstallOgKvartal: ÅrstallOgKvartal): Int {
        return transaction {
            deleteWhere {
                årstall less årstallOgKvartal.årstall or ((årstall eq årstallOgKvartal.årstall) and (kvartal less årstallOgKvartal.kvartal))
            }
        }
    }
}