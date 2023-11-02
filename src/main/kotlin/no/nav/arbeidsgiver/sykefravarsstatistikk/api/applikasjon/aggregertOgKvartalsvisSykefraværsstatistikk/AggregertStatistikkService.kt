package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Sykefraværsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.UmaskertSykefraværForEttKvartalMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.TilgangskontrollService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class AggregertStatistikkService(
    private val varighetRepository: VarighetRepository,
    private val tilgangskontrollService: TilgangskontrollService,
    private val enhetsregisteretClient: EnhetsregisteretClient,
    private val importtidspunktRepository: ImporttidspunktRepository,
    private val sykefravarStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository,
    private val sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository,
    private val sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository,
    private val sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository,
    private val sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    sealed class HentAggregertStatistikkFeil {
        data object BrukerManglerTilgang : HentAggregertStatistikkFeil()
        data object FeilVedKallTilEnhetsregisteret : HentAggregertStatistikkFeil()
        data object UnderenhetErIkkeNæringsdrivende : HentAggregertStatistikkFeil()
    }

    fun hentAggregertStatistikk(
        orgnr: Orgnr
    ): Either<HentAggregertStatistikkFeil, AggregertStatistikkJson> {
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
        val gradertSykefravær = hentGradertSykefraværAlleKategorier(virksomhet)
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

    private fun hentGradertSykefraværAlleKategorier(virksomhet: Virksomhet): Sykefraværsdata {
        val næring = virksomhet.næringskode.næring
        val maybeBransje = Bransjeprogram.finnBransje(virksomhet.næringskode)
        val data: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> =
            EnumMap(Statistikkategori::class.java)
        data[Statistikkategori.VIRKSOMHET] =
            sykefravarStatistikkVirksomhetGraderingRepository.hentForOrgnr(virksomhet.orgnr)
        if (maybeBransje.isEmpty) {
            data[Statistikkategori.NÆRING] = sykefravarStatistikkVirksomhetGraderingRepository.hentForNæring(næring)
        } else {
            data[Statistikkategori.BRANSJE] =
                sykefravarStatistikkVirksomhetGraderingRepository.hentForBransje(maybeBransje.get())
        }
        return Sykefraværsdata(data)
    }

    private fun aggregerData(
        virksomhet: Virksomhet,
        totalfravær: Sykefraværsdata,
        gradertFravær: Sykefraværsdata,
        korttidsfravær: Sykefraværsdata,
        langtidsfravær: Sykefraværsdata
    ): AggregertStatistikkJson {
        val sistePubliserteKvartal =
            importtidspunktRepository.hentNyesteImporterteKvartal()?.gjeldendePeriode
                ?: throw IllegalStateException("Fant ikke siste publiserte kvartal")
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
        return AggregertStatistikkJson(
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
        val gjeldendePeriode = importtidspunktRepository.hentNyesteImporterteKvartal()?.gjeldendePeriode
            ?: throw IllegalStateException("Fant ikke siste publiserte kvartal")

        return hentTotaltSykefraværAlleKategorier(forBedrift, (gjeldendePeriode inkludertTidligere 4))
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

    fun hentTotaltSykefraværAlleKategorier(
        virksomhet: Virksomhet, kvartaler: List<ÅrstallOgKvartal>
    ): Sykefraværsdata {

        val næring = virksomhet.næringskode.næring
        val maybeBransje = Bransjeprogram.finnBransje(virksomhet.næringskode)
        val data: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> =
            EnumMap(Statistikkategori::class.java)
        val fraÅrstallOgKvartal = kvartaler.minOf { it }

        data[Statistikkategori.VIRKSOMHET] = sykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(
            virksomhet,
            fraÅrstallOgKvartal
        )
        data[Statistikkategori.LAND] = sykefraværStatistikkLandRepository.hentForKvartaler(kvartaler)
        if (maybeBransje.isEmpty) {
            data[Statistikkategori.NÆRING] =
                sykefraværStatistikkNæringRepository.hentUmaskertSykefravær(næring, kvartaler)
        } else if (maybeBransje.get().erDefinertPåFemsiffernivå()) {
            val næringskoder = maybeBransje.get().identifikatorer.map { Næringskode(it) }
            data[Statistikkategori.BRANSJE] =
                sykefraværStatistikkNæringskodeRepository.hentForKvartaler(næringskoder, kvartaler)
        } else {
            data[Statistikkategori.BRANSJE] =
                sykefraværStatistikkNæringRepository.hentUmaskertSykefravær(næring, kvartaler)
        }
        return Sykefraværsdata(data)
    }

}

