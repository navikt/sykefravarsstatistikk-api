package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ia.felles.definisjoner.bransjer.BransjeId
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Sykefraværsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Bransjeprogram.finnBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.TilgangskontrollService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class AggregertStatistikkService(
    private val sykefraværStatistikkNæringMedVarighetRepository: SykefraværStatistikkNæringMedVarighetRepository,
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

        val brukerHarRettigheterTilVirksomhetsdata = tilgangskontrollService.brukerHarIaRettigheterIVirksomheten(orgnr)

        val totalSykefravær = hentTotalfraværSisteFemKvartaler(virksomhet, brukerHarRettigheterTilVirksomhetsdata)
        val gradertSykefravær = hentGradertSykefraværAlleKategorier(virksomhet, brukerHarRettigheterTilVirksomhetsdata)
        val korttidSykefravær = hentKortidsfravær(virksomhet, brukerHarRettigheterTilVirksomhetsdata)
        val langtidsfravær = hentLangtidsfravær(virksomhet, brukerHarRettigheterTilVirksomhetsdata)

        return aggregerData(
            virksomhet = virksomhet,
            totalfravær = totalSykefravær,
            gradertFravær = gradertSykefravær,
            korttidsfravær = korttidSykefravær,
            langtidsfravær = langtidsfravær
        ).right()
    }

    private fun hentGradertSykefraværAlleKategorier(
        virksomhet: Virksomhet,
        skalInkludereVirksomhetsdata: Boolean
    ): Sykefraværsdata {
        val næring = virksomhet.næringskode.næring
        val bransje = finnBransje(virksomhet.næringskode)

        val data: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> =
            EnumMap(Statistikkategori::class.java)

        if (skalInkludereVirksomhetsdata) {
            data[VIRKSOMHET] =
                sykefravarStatistikkVirksomhetGraderingRepository.hentForOrgnr(virksomhet.orgnr)
        }
        if (bransje == null) {
            data[NÆRING] = sykefravarStatistikkVirksomhetGraderingRepository.hentForNæring(næring)
        } else {
            data[BRANSJE] =
                sykefravarStatistikkVirksomhetGraderingRepository.hentForBransje(bransje)
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

        val prosentSisteFireKvartalerTotalt = arrayOf(
            kalkulatorTotal.fraværsprosentVirksomhet(virksomhet.navn),
            kalkulatorTotal.fraværsprosentBransjeEllerNæring(bransjeEllerNæring),
            kalkulatorTotal.fraværsprosentNorge()
        ).mapNotNull { it.getOrNull() }

        val prosentSisteFireKvartalerGradert = arrayOf(
            kalkulatorGradert.fraværsprosentVirksomhet(virksomhet.navn),
            kalkulatorGradert.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        ).mapNotNull { it.getOrNull() }

        val prosentSisteFireKvartalerKorttid = arrayOf(
            kalkulatorKorttid.fraværsprosentVirksomhet(virksomhet.navn),
            kalkulatorKorttid.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        ).mapNotNull { it.getOrNull() }

        val prosentSisteFireKvartalerLangtid = arrayOf(
            kalkulatorLangtid.fraværsprosentVirksomhet(virksomhet.navn),
            kalkulatorLangtid.fraværsprosentBransjeEllerNæring(bransjeEllerNæring)
        ).mapNotNull { it.getOrNull() }

        val trendTotalt = arrayOf(
            kalkulatorTotal.trendBransjeEllerNæring(bransjeEllerNæring)
        ).mapNotNull { it.getOrNull() }

        val tapteDagsverkTotalt = arrayOf(
            kalkulatorTotal.tapteDagsverkVirksomhet(virksomhet.navn)
        ).mapNotNull { it.getOrNull() }

        val muligeDagsverkTotalt = arrayOf(
            kalkulatorTotal.muligeDagsverkVirksomhet(virksomhet.navn)
        ).mapNotNull { it.getOrNull() }

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

    private fun hentTotalfraværSisteFemKvartaler(
        forBedrift: Virksomhet,
        skalInkludereVirksomhetsstatistikk: Boolean
    ): Sykefraværsdata {
        val gjeldendePeriode = importtidspunktRepository.hentNyesteImporterteKvartal()?.gjeldendePeriode
            ?: throw IllegalStateException("Fant ikke siste publiserte kvartal")

        return hentTotaltSykefraværAlleKategorier(
            forBedrift,
            (gjeldendePeriode inkludertTidligere 4),
            skalInkludereVirksomhetsstatistikk
        )
    }

    fun finnBransjeEllerNæring(virksomhet: Virksomhet): BransjeEllerNæring =
        finnBransje(virksomhet.næringskode)?.let {
            BransjeEllerNæring(it)
        } ?: BransjeEllerNæring(virksomhet.næringskode.næring)

    fun hentTotaltSykefraværAlleKategorier(
        virksomhet: Virksomhet, kvartaler: List<ÅrstallOgKvartal>, skalInkludereVirksomhetsstatistikk: Boolean
    ): Sykefraværsdata {

        val næring = virksomhet.næringskode.næring
        val maybeBransje = finnBransje(virksomhet.næringskode)

        val data: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> = mutableMapOf()

        if (skalInkludereVirksomhetsstatistikk) {
            data[VIRKSOMHET] = sykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(
                virksomhet,
                kvartaler
            )
        }

        data[LAND] = sykefraværStatistikkLandRepository.hentForKvartaler(kvartaler)

        if (maybeBransje == null) {
            data[NÆRING] =
                sykefraværStatistikkNæringRepository.hentForKvartaler(næring, kvartaler)
        } else if (maybeBransje.bransjeId is BransjeId.Næringskoder) {
            data[BRANSJE] =
                sykefraværStatistikkNæringskodeRepository.hentForBransje(maybeBransje, kvartaler)
                    .map { UmaskertSykefraværForEttKvartal(it) }
        } else {
            data[BRANSJE] =
                sykefraværStatistikkNæringRepository.hentForKvartaler(næring, kvartaler)
        }

        return Sykefraværsdata(data)
    }

    private fun hentLangtidsfravær(
        virksomhet: Virksomhet,
        inkluderVirksomhetsstatistikk: Boolean
    ): Sykefraværsdata {
        val næring = virksomhet.næringskode.næring
        val bransje = finnBransje(virksomhet.næringskode)
        val data: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> = mutableMapOf()

        if (inkluderVirksomhetsstatistikk) {
            data[VIRKSOMHET] = sykefravarStatistikkVirksomhetRepository.hentLangtidsfravær(virksomhet.orgnr)
        }
        if (bransje == null) {
            data[NÆRING] =
                sykefraværStatistikkNæringMedVarighetRepository.hentLangtidsfravær(næring)
        } else {
            data[BRANSJE] =
                sykefraværStatistikkNæringMedVarighetRepository.hentLangtidsfravær(bransje.bransjeId)
        }

        return Sykefraværsdata(data)
    }


    private fun hentKortidsfravær(
        virksomhet: Virksomhet,
        inkluderVirksomhetsstatistikk: Boolean
    ): Sykefraværsdata {
        val næring = virksomhet.næringskode.næring
        val bransje = finnBransje(virksomhet.næringskode)
        val data: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> = mutableMapOf()

        if (inkluderVirksomhetsstatistikk) {
            data[VIRKSOMHET] =
                sykefravarStatistikkVirksomhetRepository.hentKorttidsfravær(virksomhet.orgnr)
        }
        if (bransje == null) {
            data[NÆRING] =
                sykefraværStatistikkNæringMedVarighetRepository.hentKorttidsfravær(næring)
        } else {
            data[BRANSJE] =
                sykefraværStatistikkNæringMedVarighetRepository.hentKorttidsfravær(bransje.bransjeId)
        }

        return Sykefraværsdata(data)
    }
}

