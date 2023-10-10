package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import java.math.BigDecimal

data class UmaskertSykefraværForEttKvartalMedVarighet(
    override val årstallOgKvartal: ÅrstallOgKvartal,
    val tapteDagsverk: BigDecimal,
    val muligeDagsverk: BigDecimal,
    override val antallPersoner: Int,
    val varighet: Varighetskategori
) : UmaskertSykefraværForEttKvartal(årstallOgKvartal, tapteDagsverk, muligeDagsverk, antallPersoner)