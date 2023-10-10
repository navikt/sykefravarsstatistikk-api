package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering

import lombok.extern.slf4j.Slf4j
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransjeprogram.finnBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KvartalsvisSykefraværRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream

@Component
@Slf4j
class KvartalsvisSykefraværshistorikkService(
    private val kvartalsvisSykefraværprosentRepository: KvartalsvisSykefraværRepository
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    val SYKEFRAVÆRPROSENT_LAND_LABEL = "Norge"
    val TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER = 3

    fun hentSykefraværshistorikk(
        underenhet: Virksomhet, sektor: Sektor?
    ): MutableList<KvartalsvisSykefraværshistorikk> {
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
            .map { obj: CompletableFuture<KvartalsvisSykefraværshistorikk> -> obj.join() }
            .collect(Collectors.toList())
    }

    fun hentSykefraværshistorikk(
        underenhet: Virksomhet, overordnetEnhet: OverordnetEnhet
    ): List<KvartalsvisSykefraværshistorikk> {
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

    protected fun hentSykefraværshistorikkLand(): KvartalsvisSykefraværshistorikk {
        return KvartalsvisSykefraværshistorikk(
            Statistikkategori.LAND,
            SYKEFRAVÆRPROSENT_LAND_LABEL,
            kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand()
        )
    }

    private fun hentSykefraværshistorikkSektor(ssbSektor: Sektor?): KvartalsvisSykefraværshistorikk {
        return KvartalsvisSykefraværshistorikk(
            Statistikkategori.SEKTOR,
            ssbSektor!!.displaystring,
            kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(ssbSektor)
        )
    }

    private fun hentSykefraværshistorikkNæring(næring: Næring): KvartalsvisSykefraværshistorikk {
        return KvartalsvisSykefraværshistorikk(
            Statistikkategori.NÆRING,
            næring.navn,
            kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentNæring(næring)
        )
    }

    protected fun hentSykefraværshistorikkBransje(bransje: Bransje): KvartalsvisSykefraværshistorikk {
        return KvartalsvisSykefraværshistorikk(
            Statistikkategori.BRANSJE,
            bransje.navn,
            kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentBransje(bransje)
        )
    }

    private fun hentSykefraværshistorikkVirksomhet(
        virksomhet: Virksomhet, type: Statistikkategori
    ): KvartalsvisSykefraværshistorikk {
        return KvartalsvisSykefraværshistorikk(
            type,
            virksomhet.navn,
            kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(
                virksomhet
            )
        )
    }

    protected fun uthentingAvSykefraværshistorikkNæring(underenhet: Virksomhet): CompletableFuture<KvartalsvisSykefraværshistorikk> {
        return CompletableFuture.supplyAsync<Næring> { underenhet.næringskode.næring }
            .orTimeout(TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER.toLong(), TimeUnit.SECONDS)
            .thenCompose<KvartalsvisSykefraværshistorikk> { næring: Næring ->
                uthentingMedFeilhåndteringOgTimeout(
                    { hentSykefraværshistorikkNæring(næring) },
                    Statistikkategori.NÆRING,
                    næring.navn
                )
            }
            .handle<KvartalsvisSykefraværshistorikk> { result: KvartalsvisSykefraværshistorikk?, throwable: Throwable? ->
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
                    return@handle KvartalsvisSykefraværshistorikk(
                        Statistikkategori.NÆRING, null, listOf<SykefraværForEttKvartal>()
                    )
                }
            }
    }

    private fun uthentingMedFeilhåndteringOgTimeout(
        sykefraværshistorikkSupplier: Supplier<KvartalsvisSykefraværshistorikk>?,
        statistikkategori: Statistikkategori?,
        sykefraværshistorikkLabel: String?
    ): CompletableFuture<KvartalsvisSykefraværshistorikk> {

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
                KvartalsvisSykefraværshistorikk(
                    statistikkategori, sykefraværshistorikkLabel, emptyList()
                )
            }
    }
}
