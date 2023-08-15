package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering

import lombok.extern.slf4j.Slf4j
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.BransjeEllerNæringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværRepository
import org.springframework.stereotype.Component

@Slf4j
@Component
class SummertLegemeldtSykefraværService(
    private val sykefraværprosentRepository: SykefraværRepository,
    private val bransjeEllerNæringService: BransjeEllerNæringService
) {
    fun hentLegemeldtSykefraværsprosent(
        underenhet: Virksomhet, sistePubliserteÅrstallOgKvartal: ÅrstallOgKvartal
    ): LegemeldtSykefraværsprosent {
        val eldsteÅrstallOgKvartal = sistePubliserteÅrstallOgKvartal.minusKvartaler(3)
        val altSykefravær =
            sykefraværprosentRepository.hentUmaskertSykefravær(underenhet, eldsteÅrstallOgKvartal)
        val harData = altSykefravær.isNotEmpty()
        val summertSykefravær = summerOpp(altSykefravær)
        val erMaskert: Boolean = summertSykefravær.erMaskert
        return if (harData && !erMaskert) {
            LegemeldtSykefraværsprosent(
                Statistikkategori.VIRKSOMHET, underenhet.navn, summertSykefravær.prosent
            )
        } else {
            hentLegemeldtSykefraværsprosentUtenStatistikkForVirksomhet(
                underenhet,
                sistePubliserteÅrstallOgKvartal
            )
        }
    }

    fun hentLegemeldtSykefraværsprosentUtenStatistikkForVirksomhet(
        underenhet: Virksomhet, sistePubliserteÅrstallOgKvartal: ÅrstallOgKvartal
    ): LegemeldtSykefraværsprosent {
        val eldsteÅrstallOgKvartal = sistePubliserteÅrstallOgKvartal.minusKvartaler(3)
        val bransjeEllerNæring = bransjeEllerNæringService.bestemFraNæringskode(underenhet.næringskode)
        return if (bransjeEllerNæring.isBransje) {
            val bransje = bransjeEllerNæring.getBransje()
            val listeAvSykefraværForEttKvartalForBransje =
                sykefraværprosentRepository.hentUmaskertSykefravær(bransje, eldsteÅrstallOgKvartal)
            val summertSykefraværBransje =
                summerOpp(listeAvSykefraværForEttKvartalForBransje)
            LegemeldtSykefraværsprosent(
                bransjeEllerNæring.statistikkategori,
                bransje.navn,
                summertSykefraværBransje.prosent
            )
        } else {
            val næring = bransjeEllerNæring.næring
            val listeAvSykefraværForEttKvartalForNæring =
                sykefraværprosentRepository.hentUmaskertSykefravær(næring, eldsteÅrstallOgKvartal)
            val summertSykefraværNæring =
                summerOpp(listeAvSykefraværForEttKvartalForNæring)
            LegemeldtSykefraværsprosent(
                bransjeEllerNæring.statistikkategori,
                næring.navn,
                summertSykefraværNæring.prosent
            )
        }
    }
}

fun summerOpp(kvartalsvisSykefravær: List<UmaskertSykefraværForEttKvartal>): MaskerbartSykefravær {
    val totalTapteDagsverk = kvartalsvisSykefravær.sumOf { it.dagsverkTeller }
    val totalMuligeDagsverk = kvartalsvisSykefravær.sumOf { it.dagsverkNevner }
    val maksAntallPersoner = kvartalsvisSykefravær.maxByOrNull { it.antallPersoner }?.antallPersoner ?: 0
    val harSykefraværsdata: Boolean = kvartalsvisSykefravær.isNotEmpty()

    return MaskerbartSykefravær(totalTapteDagsverk, totalMuligeDagsverk, maksAntallPersoner, harSykefraværsdata)
}