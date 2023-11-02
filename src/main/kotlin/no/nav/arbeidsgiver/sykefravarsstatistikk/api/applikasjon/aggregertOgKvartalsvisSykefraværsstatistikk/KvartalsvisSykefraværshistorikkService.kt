package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Bransjeprogram.finnBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream

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
    val TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER = 3

    fun hentSykefraværshistorikk(
        underenhet: Virksomhet, sektor: Sektor?
    ): MutableList<KvartalsvisSykefraværshistorikkJson> {
        val bransje = finnBransje(underenhet)
        return Stream.of(
            uthentingMedFeilhåndteringOgTimeout(
                { hentSykefraværshistorikkLand() },
                Statistikkategori.LAND,
                SYKEFRAVÆRPROSENT_LAND_LABEL
            ),
            uthentingMedFeilhåndteringOgTimeout(
                { hentSykefraværshistorikkSektor(sektor) },
                Statistikkategori.SEKTOR,
                sektor!!.displaystring
            ),
            uthentingForBransjeEllerNæring(underenhet.næringskode.næring, bransje.orElse(null)),
            uthentingMedFeilhåndteringOgTimeout(
                {
                    hentSykefraværshistorikkVirksomhet(
                        underenhet, Statistikkategori.VIRKSOMHET
                    )
                },
                Statistikkategori.VIRKSOMHET,
                underenhet.navn
            )
        )
            .map { obj: CompletableFuture<KvartalsvisSykefraværshistorikkJson> -> obj.join() }
            .collect(Collectors.toList())
    }

    private fun uthentingForBransjeEllerNæring(
        næring: Næring,
        bransje: Bransje?
    ): CompletableFuture<KvartalsvisSykefraværshistorikkJson> {
        val skalHenteDataPåNæring = bransje == null || bransje.erDefinertPåTosiffernivå()

        return if (skalHenteDataPåNæring) {
            uthentingMedFeilhåndteringOgTimeout(
                { hentSykefraværshistorikkNæring(næring) },
                Statistikkategori.NÆRING,
                næring.navn
            )
        } else {
            uthentingMedFeilhåndteringOgTimeout(
                { hentSykefraværshistorikkNæringskoder(bransje!!) },
                Statistikkategori.BRANSJE,
                bransje!!.navn
            )
        }
    }

    fun hentSykefraværshistorikk(
        underenhet: Virksomhet, overordnetEnhet: OverordnetEnhet
    ): List<KvartalsvisSykefraværshistorikkJson> {
        val historikkForOverordnetEnhet = uthentingMedFeilhåndteringOgTimeout(
            {
                hentSykefraværshistorikkVirksomhet(
                    overordnetEnhet, Statistikkategori.OVERORDNET_ENHET
                )
            },
            Statistikkategori.OVERORDNET_ENHET,
            underenhet.navn
        )
            .join()
        val kvartalsvisSykefraværshistorikkListe = hentSykefraværshistorikk(underenhet, overordnetEnhet.sektor)
        kvartalsvisSykefraværshistorikkListe.add(historikkForOverordnetEnhet)
        return kvartalsvisSykefraværshistorikkListe
    }

    private fun hentSykefraværshistorikkLand(): KvartalsvisSykefraværshistorikkJson {
        return KvartalsvisSykefraværshistorikkJson(
            Statistikkategori.LAND,
            SYKEFRAVÆRPROSENT_LAND_LABEL,
            sykefraværStatistikkLandRepository.hentAlt()
        )
    }

    private fun hentSykefraværshistorikkSektor(ssbSektor: Sektor?): KvartalsvisSykefraværshistorikkJson {
        return KvartalsvisSykefraværshistorikkJson(
            Statistikkategori.SEKTOR,
            ssbSektor!!.displaystring,
            sykefraværStatistikkSektorRepository.hentKvartalsvisSykefraværprosent(ssbSektor)
        )
    }

    private fun hentSykefraværshistorikkNæring(næring: Næring): KvartalsvisSykefraværshistorikkJson {
        return KvartalsvisSykefraværshistorikkJson(
            Statistikkategori.NÆRING,
            næring.navn,
            sykefraværStatistikkNæringRepository.hentKvartalsvisSykefraværprosent(næring)
        )
    }

    private fun hentSykefraværshistorikkNæringskoder(bransje: Bransje): KvartalsvisSykefraværshistorikkJson {
        return KvartalsvisSykefraværshistorikkJson(
            Statistikkategori.BRANSJE,
            bransje.navn,
            sykefraværStatistikkNæringskodeRepository.hentKvartalsvisSykefraværprosent(bransje)
        )
    }

    private fun hentSykefraværshistorikkVirksomhet(
        virksomhet: Virksomhet, type: Statistikkategori
    ): KvartalsvisSykefraværshistorikkJson {
        return KvartalsvisSykefraværshistorikkJson(
            type,
            virksomhet.navn,
            sykefraværStatistikkVirksomhetRepository.hentAlt(virksomhet.orgnr)
        )
    }

    private fun uthentingMedFeilhåndteringOgTimeout(
        sykefraværshistorikkSupplier: Supplier<KvartalsvisSykefraværshistorikkJson>?,
        statistikkategori: Statistikkategori?,
        sykefraværshistorikkLabel: String?
    ): CompletableFuture<KvartalsvisSykefraværshistorikkJson> {

        return CompletableFuture.supplyAsync(sykefraværshistorikkSupplier)
            .orTimeout(TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER.toLong(), TimeUnit.SECONDS)
            .exceptionally { e: Throwable ->
                log.warn(
                    String.format(
                        "Fikk '%s' ved uthenting av sykefravarsstatistikk '%s'. "
                                + "Returnerer en tom liste",
                        e.message, statistikkategori
                    ),
                    e
                )
                KvartalsvisSykefraværshistorikkJson(
                    statistikkategori, sykefraværshistorikkLabel, emptyList()
                )
            }
    }
}
