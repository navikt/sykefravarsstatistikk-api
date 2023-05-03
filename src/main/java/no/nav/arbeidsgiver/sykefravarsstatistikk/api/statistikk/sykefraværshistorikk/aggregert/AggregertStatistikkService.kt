package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert

import io.vavr.control.Either
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet
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
    fun hentAggregertStatistikk(
        orgnr: Orgnr
    ): Either<TilgangskontrollException, AggregertStatistikkDto> {
        if (!tilgangskontrollService.brukerRepresentererVirksomheten(orgnr)) {
            return Either.left(
                TilgangskontrollException("Bruker mangler tilgang til denne virksomheten")
            )
        }
        val virksomhet =
            enhetsregisteretClient.hentUnderenhet(orgnr).getOrNull() ?: // TODO: Legg til mer granulær feilmelding
            return Either.left(TilgangskontrollException("Fant ikke virksomhet med orgnr $orgnr"))
        val totalSykefravær = hentTotalfraværSisteFemKvartaler(virksomhet)
        val gradertSykefravær = hentGradertSykefravær(virksomhet)
        val korttidSykefravær = hentKorttidsfravær(virksomhet)
        val langtidsfravær = hentLangtidsfravær(virksomhet)
        if (tilgangskontrollService.brukerManglerIaRettigheterIVirksomheten(orgnr)) {
            totalSykefravær.filtrerBortVirksomhetsdata()
            gradertSykefravær.filtrerBortVirksomhetsdata()
            korttidSykefravær.filtrerBortVirksomhetsdata()
            langtidsfravær.filtrerBortVirksomhetsdata()
        }
        return Either.right(
            aggregerData(
                virksomhet, totalSykefravær, gradertSykefravær, korttidSykefravær, langtidsfravær
            )
        )
    }

    private fun aggregerData(
        virksomhet: Underenhet,
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

    private fun hentTotalfraværSisteFemKvartaler(forBedrift: Underenhet): Sykefraværsdata {
        return sykefraværprosentRepository.hentUmaskertSykefraværAlleKategorier(
            forBedrift, publiseringsdatoerService.hentSistePubliserteKvartal().minusKvartaler(4)
        )
    }

    private fun hentGradertSykefravær(virksomhet: Underenhet): Sykefraværsdata {
        return graderingRepository.hentGradertSykefraværAlleKategorier(virksomhet)
    }

    private fun hentKorttidsfravær(virksomhet: Underenhet): Sykefraværsdata {
        return hentLangtidsEllerKorttidsfravær(virksomhet) {
            it.varighet.erKorttidVarighet() || it.varighet.erTotalvarighet()
        }
    }

    private fun hentLangtidsfravær(virksomhet: Underenhet): Sykefraværsdata {
        return hentLangtidsEllerKorttidsfravær(virksomhet) {
            it.varighet.erLangtidVarighet() || it.varighet.erTotalvarighet()
        }
    }

    private fun hentLangtidsEllerKorttidsfravær(
        virksomhet: Underenhet, entenLangtidEllerKorttid: (UmaskertSykefraværForEttKvartalMedVarighet) -> Boolean
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

