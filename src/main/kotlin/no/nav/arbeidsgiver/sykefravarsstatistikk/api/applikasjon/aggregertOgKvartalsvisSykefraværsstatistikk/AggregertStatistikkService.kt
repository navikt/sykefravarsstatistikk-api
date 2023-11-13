package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ia.felles.definisjoner.bransjer.BransjeId
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Sykefraværsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Bransjeprogram.finnBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.TilgangskontrollService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AggregertStatistikkService(
    private val næringskodeMedVarighetRepository: SykefraværStatistikkNæringskodeMedVarighetRepository,
    private val tilgangskontrollService: TilgangskontrollService,
    private val enhetsregisteretClient: EnhetsregisteretClient,
    private val importtidspunktRepository: ImporttidspunktRepository,
    private val virksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository,
    private val virksomhetRepository: SykefravarStatistikkVirksomhetRepository,
    private val landRepository: SykefraværStatistikkLandRepository,
    private val næringRepository: SykefraværStatistikkNæringRepository,
    private val næringskodeRepository: SykefraværStatistikkNæringskodeRepository,
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

        val gjeldendePeriode = importtidspunktRepository.hentNyesteImporterteKvartal()?.gjeldendePeriode
            ?: throw IllegalStateException("Fant ikke siste publiserte kvartal")
        val aggregeringskategorier = buildList {
            add(Aggregeringskategorier.Land)
            if (brukerHarRettigheterTilVirksomhetsdata) {
                add(Aggregeringskategorier.Virksomhet(virksomhet))
            }

            val bransje = finnBransje(virksomhet.næringskode)
            if (bransje == null) {
                add(Aggregeringskategorier.Næring(virksomhet.næringskode.næring))
            } else {
                add(Aggregeringskategorier.Bransje(bransje))
            }
        }

        val totalSykefravær = hentTotaltSykefraværAlleKategorier(
            (gjeldendePeriode inkludertTidligere 4),
            aggregeringskategorier
        )
        val gradertSykefravær = hentGradertSykefraværAlleKategorier(aggregeringskategorier)
        val korttidSykefravær = hentKortidsfravær(aggregeringskategorier)
        val langtidsfravær = hentLangtidsfravær(aggregeringskategorier)

        return aggregerData(
            virksomhet = virksomhet,
            totalfravær = totalSykefravær,
            gradertFravær = gradertSykefravær,
            korttidsfravær = korttidSykefravær,
            langtidsfravær = langtidsfravær
        ).right()
    }

    private fun hentGradertSykefraværAlleKategorier(
        aggregerbare: List<Aggregeringskategorier>
    ): Sykefraværsdata {
        val data = aggregerbare.mapNotNull {
            when (it) {
                Aggregeringskategorier.Land -> null
                is Aggregeringskategorier.Næring -> it to virksomhetGraderingRepository.hentForNæring(it.næring)
                is Aggregeringskategorier.Bransje -> it to virksomhetGraderingRepository.hentForBransje(it.bransje)
                is Aggregeringskategorier.Virksomhet -> it to virksomhetGraderingRepository.hentForOrgnr(it.virksomhet.orgnr)
            }
        }.toMap()

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

    fun finnBransjeEllerNæring(virksomhet: Virksomhet): BransjeEllerNæring =
        finnBransje(virksomhet.næringskode)?.let {
            BransjeEllerNæring(it)
        } ?: BransjeEllerNæring(virksomhet.næringskode.næring)

    fun hentTotaltSykefraværAlleKategorier(
        kvartaler: List<ÅrstallOgKvartal>, kategorier: List<Aggregeringskategorier>
    ): Sykefraværsdata {
        val data = kategorier.associateWith {
            when (it) {
                Aggregeringskategorier.Land -> landRepository.hentForKvartaler(kvartaler)
                is Aggregeringskategorier.Næring -> næringRepository.hentForKvartaler(it.næring, kvartaler)
                is Aggregeringskategorier.Virksomhet -> virksomhetRepository.hentUmaskertSykefravær(
                    it.virksomhet,
                    kvartaler
                )

                is Aggregeringskategorier.Bransje -> when (it.bransje.bransjeId) {
                    is BransjeId.Næring -> næringRepository.hentForKvartaler(
                        Næring((it.bransje.bransjeId as BransjeId.Næring).næring),
                        kvartaler
                    )

                    is BransjeId.Næringskoder -> næringskodeRepository.hentForBransje(
                        it.bransje,
                        kvartaler
                    )
                }
            }
        }

        return Sykefraværsdata(data)
    }

    private fun hentLangtidsfravær(
        aggregeringskategorier: List<Aggregeringskategorier>
    ): Sykefraværsdata {
        val data = aggregeringskategorier.mapNotNull {
            when (it) {
                Aggregeringskategorier.Land -> null
                is Aggregeringskategorier.Bransje -> it to næringskodeMedVarighetRepository.hentLangtidsfravær(it.bransje.bransjeId)
                is Aggregeringskategorier.Næring -> it to næringskodeMedVarighetRepository.hentLangtidsfravær(it.næring)
                is Aggregeringskategorier.Virksomhet -> it to virksomhetRepository.hentLangtidsfravær(it.virksomhet.orgnr)
            }
        }.toMap()

        return Sykefraværsdata(data)
    }


    private fun hentKortidsfravær(
        aggregeringskategorier: List<Aggregeringskategorier>
    ): Sykefraværsdata {
        val data = aggregeringskategorier.mapNotNull {
            when (it) {
                Aggregeringskategorier.Land -> null
                is Aggregeringskategorier.Bransje -> it to næringskodeMedVarighetRepository.hentKorttidsfravær(it.bransje.bransjeId)
                is Aggregeringskategorier.Næring -> it to næringskodeMedVarighetRepository.hentKorttidsfravær(it.næring)
                is Aggregeringskategorier.Virksomhet -> it to virksomhetRepository.hentKorttidsfravær(it.virksomhet.orgnr)
            }
        }.toMap()

        return Sykefraværsdata(data)
    }
}

