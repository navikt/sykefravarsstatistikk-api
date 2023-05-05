package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.EitherUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.GraderingRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.VarighetRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AggregertStatistikkService(
    private val sykefraværprosentRepository: SykefraværRepository,
    private val graderingRepository: GraderingRepository,
    private val varighetRepository: VarighetRepository,
    private val bransjeEllerNæringService: BransjeEllerNæringService,
    private val tilgangskontrollService: TilgangskontrollService,
    private val enhetsregisteretClient: EnhetsregisteretClient,
    private val publiseringsdatoerService: PubliseringsdatoerService
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    fun hentAggregertStatistikk(
        orgnr: Orgnr
    ): Either<TilgangskontrollException, AggregertStatistikkDto> {
        if (!tilgangskontrollService.brukerRepresentererVirksomheten(orgnr)) {
            return TilgangskontrollException("Bruker mangler tilgang til denne virksomheten").left()
        }
        val virksomhet = enhetsregisteretClient.hentUnderenhet(orgnr).fold(
            {
                return TilgangskontrollException("Fant ikke virksomhet med orgnr $orgnr").left()
            },
            {
                when (it) {
                    is Underenhet.IkkeNæringsdrivende -> {
                        log.info("Underenhet {} er ikke næringsdrivende", orgnr.verdi)
                        return TilgangskontrollException("Underenhet $orgnr er ikke næringsdrivende").left()
                    }

                    is Underenhet.Næringsdrivende -> it
                }
            }
        )
        val totalSykefravær = hentTotalfraværSisteFemKvartaler(virksomhet)
        val gradertSykefravær = hentGradertSykefravær(virksomhet)
        val korttidSykefravær = hentKorttidsfravær(virksomhet)
        val langtidsfravær = hentLangtidsfravær(virksomhet)

        if (!tilgangskontrollService.brukerHarIaRettigheterIVirksomheten(orgnr)) {
            totalSykefravær.filtrerBortVirksomhetsdata()
            gradertSykefravær.filtrerBortVirksomhetsdata()
            korttidSykefravær.filtrerBortVirksomhetsdata()
            langtidsfravær.filtrerBortVirksomhetsdata()
        }
        return aggregerData(
            virksomhet, totalSykefravær, gradertSykefravær, korttidSykefravær, langtidsfravær
        ).right()

    }

    private fun aggregerData(
        virksomhet: Virksomhet,
        totalfravær: Sykefraværsdata,
        gradertFravær: Sykefraværsdata,
        korttidsfravær: Sykefraværsdata,
        langtidsfravær: Sykefraværsdata
    ): AggregertStatistikkDto {
        val sistePubliserteKvartal = publiseringsdatoerService.hentSistePubliserteKvartal()
        val kalkulatorTotal = Aggregeringskalkulator(totalfravær, sistePubliserteKvartal)
        val kalkulatorGradert = Aggregeringskalkulator(gradertFravær, sistePubliserteKvartal)
        val kalkulatorKorttid = Aggregeringskalkulator(korttidsfravær, sistePubliserteKvartal)
        val kalkulatorLangtid = Aggregeringskalkulator(langtidsfravær, sistePubliserteKvartal)
        val bransjeEllerNæring = bransjeEllerNæringService.finnBransje(virksomhet)
        val prosentSisteFireKvartalerTotalt = EitherUtils.getRightsAndLogLefts(
            kalkulatorTotal.fraværsprosentVirksomhet(virksomhet.navn),
            kalkulatorTotal.fraværsprosentBransjeEllerNæring(bransjeEllerNæring),
            kalkulatorTotal.fraværsprosentNorge()
        )
        val prosentSisteFireKvartalerGradert = EitherUtils.getRightsAndLogLefts(
            kalkulatorGradert.fraværsprosentVirksomhet(virksomhet.navn),
            kalkulatorGradert.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        )
        val prosentSisteFireKvartalerKorttid = EitherUtils.getRightsAndLogLefts(
            kalkulatorKorttid.fraværsprosentVirksomhet(virksomhet.navn),
            kalkulatorKorttid.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        )
        val prosentSisteFireKvartalerLangtid = EitherUtils.getRightsAndLogLefts(
            kalkulatorLangtid.fraværsprosentVirksomhet(virksomhet.navn),
            kalkulatorLangtid.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        )
        val trendTotalt = EitherUtils.getRightsAndLogLefts(
            kalkulatorTotal.trendBransjeEllerNæring(bransjeEllerNæring)
        )
        val tapteDagsverkTotalt = EitherUtils.getRightsAndLogLefts(
            kalkulatorTotal.tapteDagsverkVirksomhet(virksomhet.navn)
        )
        val muligeDagsverkTotalt = EitherUtils.getRightsAndLogLefts(
            kalkulatorTotal.muligeDagsverkVirksomhet(virksomhet.navn)
        )
        return AggregertStatistikkDto(
            prosentSisteFireKvartalerTotalt,
            prosentSisteFireKvartalerGradert,
            prosentSisteFireKvartalerKorttid,
            prosentSisteFireKvartalerLangtid,
            trendTotalt,
            tapteDagsverkTotalt,
            muligeDagsverkTotalt
        )
    }

    private fun hentTotalfraværSisteFemKvartaler(forBedrift: Virksomhet): Sykefraværsdata {
        return sykefraværprosentRepository.hentUmaskertSykefraværAlleKategorier(
            forBedrift, publiseringsdatoerService.hentSistePubliserteKvartal().minusKvartaler(4)
        )
    }

    private fun hentGradertSykefravær(virksomhet: Virksomhet): Sykefraværsdata {
        return graderingRepository.hentGradertSykefraværAlleKategorier(virksomhet)
    }

    private fun hentKorttidsfravær(virksomhet: Virksomhet): Sykefraværsdata {
        return hentLangtidsEllerKorttidsfravær(virksomhet) {
            it.varighet.erKorttidVarighet() || it.varighet.erTotalvarighet()
        }
    }

    private fun hentLangtidsfravær(virksomhet: Virksomhet): Sykefraværsdata {
        return hentLangtidsEllerKorttidsfravær(virksomhet) {
            it.varighet.erLangtidVarighet() || it.varighet.erTotalvarighet()
        }
    }

    private fun hentLangtidsEllerKorttidsfravær(
        virksomhet: Virksomhet, entenLangtidEllerKorttid: (UmaskertSykefraværForEttKvartalMedVarighet) -> Boolean
    ) = Sykefraværsdata(
        varighetRepository.hentUmaskertSykefraværMedVarighetAlleKategorier(virksomhet)
            .mapValues { (_, statistikk) ->
                statistikk.filter(entenLangtidEllerKorttid).let(::grupperOgSummerHvertKvartal)
            }.toMutableMap()
    )


    private fun grupperOgSummerHvertKvartal(
        fraværFlereKvartaler: List<UmaskertSykefraværForEttKvartal>
    ): List<UmaskertSykefraværForEttKvartal> {
        return fraværFlereKvartaler.groupBy(UmaskertSykefraværForEttKvartal::årstallOgKvartal)
            .mapValues { (_, fraværFlereKvartaler) -> fraværFlereKvartaler.reduce(UmaskertSykefraværForEttKvartal::add) }
            .values.toList()
    }
}

