package testUtils

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefravarStatistikkVirksomhetGraderingRepository
import org.jetbrains.exposed.sql.insert
import java.math.BigDecimal


fun SykefravarStatistikkVirksomhetGraderingRepository.insertData(
    orgnr: String,
    næring: String,
    næringskode: String,
    rectype: String,
    årstallOgKvartal: ÅrstallOgKvartal,
    antallPersoner: Int,
    tapteDagsverkGradertSykemelding: BigDecimal,
    tapteDagsverk: BigDecimal,
    muligeDagsverk: BigDecimal
) {
    return transaction {
        insert {
            it[this.orgnr] = orgnr
            it[this.næring] = næring
            it[this.næringskode] = næringskode
            it[this.årstall] = årstallOgKvartal.årstall
            it[this.kvartal] = årstallOgKvartal.kvartal
            it[this.antallGraderteSykemeldinger] = 0
            it[this.tapteDagsverkGradertSykemelding] = tapteDagsverkGradertSykemelding.toFloat()
            it[this.antallSykemeldinger] = 0
            it[this.antallPersoner] = antallPersoner
            it[this.tapteDagsverk] = tapteDagsverk.toFloat()
            it[this.muligeDagsverk] = muligeDagsverk.toFloat()
            it[this.rectype] = rectype
        }
    }
}
