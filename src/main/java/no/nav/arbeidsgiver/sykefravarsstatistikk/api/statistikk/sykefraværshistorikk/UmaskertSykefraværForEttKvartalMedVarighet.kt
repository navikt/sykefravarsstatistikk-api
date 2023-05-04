package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori
import java.math.BigDecimal

class UmaskertSykefraværForEttKvartalMedVarighet(
    val årstallOgKvartal: ÅrstallOgKvartal?,
    val tapteDagsverk: BigDecimal?,
    val muligeDagsverk: BigDecimal?,
    val antallPersoner: Int,
    val varighet: Varighetskategori
) : UmaskertSykefraværForEttKvartal(årstallOgKvartal, tapteDagsverk, muligeDagsverk, antallPersoner)