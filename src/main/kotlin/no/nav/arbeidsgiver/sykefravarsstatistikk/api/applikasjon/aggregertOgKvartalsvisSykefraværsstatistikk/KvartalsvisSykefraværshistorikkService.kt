package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Bransjeprogram.finnBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KvartalsvisSykefraværRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefravarStatistikkVirksomhetRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværSektorRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkLandRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream

@Component
class KvartalsvisSykefraværshistorikkService(
    private val kvartalsvisSykefraværprosentRepository: KvartalsvisSykefraværRepository,
    private val sykefraværStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository,
    private val sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository,
    private val sykefraværSektorRepository: SykefraværSektorRepository,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    val SYKEFRAVÆRPROSENT_LAND_LABEL = "Norge"
    val TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER = 3

    fun hentSykefraværshistorikk(
        underenhet: Virksomhet, sektor: Sektor?
    ): MutableList<KvartalsvisSykefraværshistorikkJson> {
        val bransje = finnBransje(underenhet)
        val skalHenteDataPåNæring = bransje.isEmpty || bransje.get().erDefinertPåTosiffernivå()
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
            if (skalHenteDataPåNæring) uthentingAvSykefraværshistorikkNæring(underenhet) else uthentingMedFeilhåndteringOgTimeout(
                { hentSykefraværshistorikkBransje(bransje.get()) },
                Statistikkategori.BRANSJE,
                bransje.get().navn
            ),
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

    protected fun hentSykefraværshistorikkLand(): KvartalsvisSykefraværshistorikkJson {
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
            sykefraværSektorRepository.hentKvartalsvisSykefraværprosent(ssbSektor)
        )
    }

    private fun hentSykefraværshistorikkNæring(næring: Næring): KvartalsvisSykefraværshistorikkJson {
        return KvartalsvisSykefraværshistorikkJson(
            Statistikkategori.NÆRING,
            næring.navn,
            kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentNæring(næring)
        )
    }

    private fun hentSykefraværshistorikkBransje(bransje: Bransje): KvartalsvisSykefraværshistorikkJson {
        return KvartalsvisSykefraværshistorikkJson(
            Statistikkategori.BRANSJE,
            bransje.navn,
            kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentBransje(bransje)
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

    protected fun uthentingAvSykefraværshistorikkNæring(underenhet: Virksomhet): CompletableFuture<KvartalsvisSykefraværshistorikkJson> {
        return CompletableFuture.supplyAsync<Næring> { underenhet.næringskode.næring }
            .orTimeout(TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER.toLong(), TimeUnit.SECONDS)
            .thenCompose { næring: Næring ->
                uthentingMedFeilhåndteringOgTimeout(
                    { hentSykefraværshistorikkNæring(næring) },
                    Statistikkategori.NÆRING,
                    næring.navn
                )
            }
            .handle { result: KvartalsvisSykefraværshistorikkJson?, throwable: Throwable? ->
                if (throwable == null) {
                    return@handle result
                } else {
                    log.warn(
                        String.format(
                            "Fikk '%s' ved uthenting av næring '%s'. " + "Returnerer en tom liste",
                            throwable.message, underenhet.næringskode.næring.tosifferIdentifikator
                        ),
                        throwable
                    )
                    return@handle KvartalsvisSykefraværshistorikkJson(
                        Statistikkategori.NÆRING, null, listOf<SykefraværForEttKvartal>()
                    )
                }
            }
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
