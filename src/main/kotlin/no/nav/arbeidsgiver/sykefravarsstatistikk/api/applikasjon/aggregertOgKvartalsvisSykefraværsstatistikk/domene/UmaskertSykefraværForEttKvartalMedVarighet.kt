package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import java.math.BigDecimal

data class UmaskertSykefraværForEttKvartalMedVarighet(
    override val årstallOgKvartal: ÅrstallOgKvartal,
    val tapteDagsverk: BigDecimal,
    val muligeDagsverk: BigDecimal,
    override val antallPersoner: Int,
    val varighet: Varighetskategori
) : UmaskertSykefraværForEttKvartal(årstallOgKvartal, tapteDagsverk, muligeDagsverk, antallPersoner)