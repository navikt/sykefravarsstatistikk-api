package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import java.math.BigDecimal

class UmaskertSykefraværForEttKvartalMedVarighet(
    val årstallOgKvartal: ÅrstallOgKvartal?,
    val tapteDagsverk: BigDecimal?,
    val muligeDagsverk: BigDecimal?,
    val antallPersoner: Int,
    val varighet: Varighetskategori
) : UmaskertSykefraværForEttKvartal(årstallOgKvartal, tapteDagsverk, muligeDagsverk, antallPersoner)