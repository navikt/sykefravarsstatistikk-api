package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.TilgangskontrollService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.BransjeEllerNæring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransjeprogram
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.GraderingRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VarighetRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AggregertStatistikkService(
    private val sykefraværprosentRepository: SykefraværRepository,
    private val graderingRepository: GraderingRepository,
    private val varighetRepository: VarighetRepository,
    private val tilgangskontrollService: TilgangskontrollService,
    private val enhetsregisteretClient: EnhetsregisteretClient,
    private val publiseringsdatoerService: PubliseringsdatoerService
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    sealed class HentAggregertStatistikkFeil {
        object BrukerManglerTilgang : HentAggregertStatistikkFeil()
        object FeilVedKallTilEnhetsregisteret : HentAggregertStatistikkFeil()
        object UnderenhetErIkkeNæringsdrivende : HentAggregertStatistikkFeil()
    }

    fun hentAggregertStatistikk(
        orgnr: Orgnr
    ): Either<HentAggregertStatistikkFeil, AggregertStatistikkDto> {
        if (!tilgangskontrollService.brukerRepresentererVirksomheten(orgnr)) {
            log.warn("Bruker mangler tilgang til denne virksomheten {}", orgnr.verdi)
            return HentAggregertStatistikkFeil.BrukerManglerTilgang.left()
        }
        val virksomhet = enhetsregisteretClient.hentUnderenhet(orgnr).fold(
            {
                return HentAggregertStatistikkFeil.FeilVedKallTilEnhetsregisteret.left()
            },
            {
                when (it) {
                    is Underenhet.IkkeNæringsdrivende -> {
                        log.info("Underenhet {} er ikke næringsdrivende", orgnr.verdi)
                        return HentAggregertStatistikkFeil.UnderenhetErIkkeNæringsdrivende.left()
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
        val bransjeEllerNæring = finnBransjeEllerNæring(virksomhet)

        val prosentSisteFireKvartalerTotalt = filterRights(
            kalkulatorTotal.fraværsprosentVirksomhet(virksomhet.navn),
            kalkulatorTotal.fraværsprosentBransjeEllerNæring(bransjeEllerNæring),
            kalkulatorTotal.fraværsprosentNorge()
        )
        val prosentSisteFireKvartalerGradert = filterRights(
            kalkulatorGradert.fraværsprosentVirksomhet(virksomhet.navn),
            kalkulatorGradert.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        )
        val prosentSisteFireKvartalerKorttid = filterRights(
            kalkulatorKorttid.fraværsprosentVirksomhet(virksomhet.navn),
            kalkulatorKorttid.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        )
        val prosentSisteFireKvartalerLangtid = filterRights(
            kalkulatorLangtid.fraværsprosentVirksomhet(virksomhet.navn),
            kalkulatorLangtid.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        )
        val trendTotalt = filterRights(
            kalkulatorTotal.trendBransjeEllerNæring(bransjeEllerNæring)
        )
        val tapteDagsverkTotalt = filterRights(
            kalkulatorTotal.tapteDagsverkVirksomhet(virksomhet.navn)
        )
        val muligeDagsverkTotalt = filterRights(
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
        return sykefraværprosentRepository.hentTotaltSykefraværAlleKategorier(
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

    private fun <L, R> filterRights(vararg leftsAndRights: Either<L, R>): List<R> = leftsAndRights
        .mapNotNull { it.getOrNull() }

    fun finnBransjeEllerNæring(virksomhet: Virksomhet): BransjeEllerNæring {
        val maybeBransje = Bransjeprogram.finnBransje(virksomhet)
        return maybeBransje
            .map { BransjeEllerNæring(it) }
            .orElseGet {
                val kode = virksomhet.næringskode.næring.tosifferIdentifikator
                BransjeEllerNæring(Næring(kode))
            }
    }
}
