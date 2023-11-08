package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import ia.felles.definisjoner.bransjer.Bransje
import ia.felles.definisjoner.bransjer.BransjeId
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Bransjeprogram.finnBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.seconds

@Component
class KvartalsvisSykefraværshistorikkService(
    private val sykefraværStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository,
    private val sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository,
    private val sykefraværStatistikkSektorRepository: SykefraværStatistikkSektorRepository,
    private val sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository,
    private val sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    val SYKEFRAVÆRPROSENT_LAND_LABEL = "Norge"

    fun hentSykefraværshistorikk(
        underenhet: Virksomhet, sektor: Sektor?
    ): List<KvartalsvisSykefraværshistorikkJson> {
        val bransje = finnBransje(underenhet.næringskode)
        return runBlocking {
            listOf(
                uthentingMedFeilhåndteringOgTimeout(
                    {
                        KvartalsvisSykefraværshistorikkJson(
                            Statistikkategori.LAND,
                            SYKEFRAVÆRPROSENT_LAND_LABEL,
                            sykefraværStatistikkLandRepository.hentAlt()
                        )
                    }, Statistikkategori.LAND, SYKEFRAVÆRPROSENT_LAND_LABEL
                ),
                uthentingMedFeilhåndteringOgTimeout(
                    {
                        KvartalsvisSykefraværshistorikkJson(
                            Statistikkategori.SEKTOR,
                            sektor!!.displaystring,
                            sykefraværStatistikkSektorRepository.hentKvartalsvisSykefraværprosent(sektor)
                        )
                    }, Statistikkategori.SEKTOR, sektor!!.displaystring
                ),
                uthentingForBransjeEllerNæring(underenhet.næringskode.næring, bransje),
                uthentingMedFeilhåndteringOgTimeout(
                    {
                        hentSykefraværshistorikkVirksomhet(
                            underenhet, Statistikkategori.VIRKSOMHET
                        )
                    }, Statistikkategori.VIRKSOMHET, underenhet.navn
                )
            )
        }
    }

    fun hentSykefraværshistorikk(
        underenhet: Virksomhet, overordnetEnhet: OverordnetEnhet
    ): List<KvartalsvisSykefraværshistorikkJson> {
        val historikkForOverordnetEnhet = runBlocking {
            uthentingMedFeilhåndteringOgTimeout(
                {
                    hentSykefraværshistorikkVirksomhet(
                        overordnetEnhet, Statistikkategori.OVERORDNET_ENHET
                    )
                }, Statistikkategori.OVERORDNET_ENHET, underenhet.navn
            )
        }
        return hentSykefraværshistorikk(underenhet, overordnetEnhet.sektor) + historikkForOverordnetEnhet
    }

    private suspend fun uthentingForBransjeEllerNæring(
        næring: Næring, bransje: Bransje?
    ): KvartalsvisSykefraværshistorikkJson {
        return bransje?.bransjeId.let { bransjeId ->
            when (bransjeId) {
                null, is BransjeId.Næring -> uthentingMedFeilhåndteringOgTimeout(
                    {
                        KvartalsvisSykefraværshistorikkJson(
                            Statistikkategori.NÆRING,
                            næring.navn,
                            sykefraværStatistikkNæringRepository.hentKvartalsvisSykefraværprosent(næring)
                        )
                    }, Statistikkategori.NÆRING, næring.navn
                )

                is BransjeId.Næringskoder -> uthentingMedFeilhåndteringOgTimeout(
                    {
                        KvartalsvisSykefraværshistorikkJson(
                            Statistikkategori.BRANSJE,
                            bransje!!.navn,
                            sykefraværStatistikkNæringskodeRepository.hentKvartalsvisSykefraværprosent(bransjeId.næringskoder.map {
                                Næringskode(
                                    it
                                )
                            })
                        )
                    }, Statistikkategori.BRANSJE, bransje!!.navn
                )
            }
        }
    }

    private fun hentSykefraværshistorikkVirksomhet(
        virksomhet: Virksomhet, type: Statistikkategori
    ): KvartalsvisSykefraværshistorikkJson {
        return KvartalsvisSykefraværshistorikkJson(
            type, virksomhet.navn, sykefraværStatistikkVirksomhetRepository.hentAlt(virksomhet.orgnr)
        )
    }

    private suspend fun uthentingMedFeilhåndteringOgTimeout(
        sykefraværshistorikkSupplier: suspend () -> KvartalsvisSykefraværshistorikkJson,
        statistikkategori: Statistikkategori,
        sykefraværshistorikkLabel: String
    ): KvartalsvisSykefraværshistorikkJson {
        return try {
            withTimeout(3.seconds) {
                sykefraværshistorikkSupplier()
            }
        } catch (e: Throwable) {
            log.warn(
                "Fikk '${e.message}' ved uthenting av sykefravarsstatistikk '${statistikkategori}'. " +
                        "Returnerer en tom liste", e
            )
            KvartalsvisSykefraværshistorikkJson(
                statistikkategori, sykefraværshistorikkLabel, emptyList()
            )
        }
    }
}
