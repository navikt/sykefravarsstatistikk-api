package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.SektorMappingService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

@Slf4j
@Component
public class KvartalsvisSykefraværshistorikkService {

    public static final int TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER = 3;
    public static final int LENGDE_PÅ_NÆRINGSKODE_AV_BRANSJENIVÅ = 5;
    public static final String SYKEFRAVÆRPROSENT_LAND_LABEL = "Norge";

    private final KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository;
    private final SektorMappingService sektorMappingService;
    private final KlassifikasjonerRepository klassifikasjonerRepository;
    private final Bransjeprogram bransjeprogram;

    public KvartalsvisSykefraværshistorikkService(
            KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository,
            SektorMappingService sektorMappingService,
            KlassifikasjonerRepository klassifikasjonerRepository,
            Bransjeprogram bransjeprogram) {
        this.kvartalsvisSykefraværprosentRepository = kvartalsvisSykefraværprosentRepository;
        this.sektorMappingService = sektorMappingService;
        this.klassifikasjonerRepository = klassifikasjonerRepository;
        this.bransjeprogram = bransjeprogram;
    }

    public List<KvartalsvisSykefraværshistorikk> hentSykefraværshistorikk(
            Underenhet underenhet,
            InstitusjonellSektorkode institusjonellSektorkode
    ) {
        Optional<Bransje> bransje = bransjeprogram.finnBransje(underenhet);
        Sektor ssbSektor = sektorMappingService.mapTilSSBSektorKode(institusjonellSektorkode);

        boolean erIBransjeprogram =
                bransje.isPresent()
                        && bransje.get().lengdePåNæringskoder() == LENGDE_PÅ_NÆRINGSKODE_AV_BRANSJENIVÅ;

        List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikkListe =
                Stream.of(
                        uthentingMedFeilhåndteringOgTimeout(
                                () -> hentSykefraværshistorikkLand(),
                                Statistikkategori.LAND,
                                SYKEFRAVÆRPROSENT_LAND_LABEL),
                        uthentingMedFeilhåndteringOgTimeout(
                                () -> hentSykefraværshistorikkSektor(ssbSektor),
                                Statistikkategori.SEKTOR,
                                ssbSektor.getNavn()),
                        erIBransjeprogram ?
                                uthentingMedFeilhåndteringOgTimeout(
                                        () -> hentSykefraværshistorikkBransje(bransje.get()),
                                        Statistikkategori.BRANSJE,
                                        bransje.get().getNavn()
                                )
                                : uthentingAvSykefraværshistorikkNæring(underenhet),
                        uthentingMedFeilhåndteringOgTimeout(
                                () -> hentSykefraværshistorikkVirksomhet(underenhet, Statistikkategori.VIRKSOMHET),
                                Statistikkategori.VIRKSOMHET,
                                underenhet.getNavn())
                )
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());

        return kvartalsvisSykefraværshistorikkListe;
    }

    public List<KvartalsvisSykefraværshistorikk> hentSykefraværshistorikk(Underenhet underenhet, OverordnetEnhet overordnetEnhet) {

        KvartalsvisSykefraværshistorikk historikkForOverordnetEnhet = uthentingMedFeilhåndteringOgTimeout(
                () -> hentSykefraværshistorikkVirksomhet(overordnetEnhet, Statistikkategori.OVERORDNET_ENHET),
                Statistikkategori.OVERORDNET_ENHET,
                underenhet.getNavn()
        ).join();

        List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikkListe = hentSykefraværshistorikk(
                underenhet,
                overordnetEnhet.getInstitusjonellSektorkode()
        );
        kvartalsvisSykefraværshistorikkListe.add(historikkForOverordnetEnhet);

        return kvartalsvisSykefraværshistorikkListe;
    }

    private KvartalsvisSykefraværshistorikk hentSykefraværshistorikkLand() {
        return byggSykefraværshistorikk(
                Statistikkategori.LAND,
                SYKEFRAVÆRPROSENT_LAND_LABEL,
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand()
        );
    }

    private KvartalsvisSykefraværshistorikk hentSykefraværshistorikkSektor(Sektor ssbSektor) {
        return byggSykefraværshistorikk(
                Statistikkategori.SEKTOR,
                ssbSektor.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(ssbSektor)
        );
    }

    private KvartalsvisSykefraværshistorikk hentSykefraværshistorikkNæring(Næring næring) {
        return byggSykefraværshistorikk(
                Statistikkategori.NÆRING,
                næring.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentNæring(næring)
        );
    }

    private KvartalsvisSykefraværshistorikk hentSykefraværshistorikkBransje(Bransje bransje) {
        return byggSykefraværshistorikk(
                Statistikkategori.BRANSJE,
                bransje.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentBransje(bransje)
        );
    }

    private KvartalsvisSykefraværshistorikk hentSykefraværshistorikkVirksomhet(
            Virksomhet virksomhet,
            Statistikkategori type
    ) {
        return byggSykefraværshistorikk(
                type,
                virksomhet.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(virksomhet)
        );
    }

    private CompletableFuture<KvartalsvisSykefraværshistorikk> uthentingAvSykefraværshistorikkNæring(Underenhet underenhet) {
        Næringskode5Siffer næring5siffer = underenhet.getNæringskode();
        return uthentingMedTimeout(
                () -> klassifikasjonerRepository.hentNæring(næring5siffer.hentNæringskode2Siffer())

        ).thenCompose(
                næring ->
                        uthentingMedFeilhåndteringOgTimeout(
                                () -> hentSykefraværshistorikkNæring(næring),
                                Statistikkategori.NÆRING,
                                næring.getNavn()
                        )
        ).handle(
                (result, throwable) -> {
                    if (throwable == null) {
                        return result;
                    } else {
                        log.warn(
                                format("Fikk '%s' ved uthenting av næring '%s'. " +
                                                "Returnerer en tom liste",
                                        throwable.getMessage(),
                                        næring5siffer.hentNæringskode2Siffer()
                                ),
                                throwable
                        );
                        return byggSykefraværshistorikk(
                                Statistikkategori.NÆRING,
                                null,
                                Collections.EMPTY_LIST
                        );
                    }
                }
        );
    }

    private static CompletableFuture<Næring> uthentingMedTimeout(Supplier<Næring> hentNæringSupplier) {
        return CompletableFuture
                .supplyAsync(hentNæringSupplier)
                .orTimeout(TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER, TimeUnit.SECONDS);
    }

    private static CompletableFuture<KvartalsvisSykefraværshistorikk> uthentingMedFeilhåndteringOgTimeout(
            Supplier<KvartalsvisSykefraværshistorikk> sykefraværshistorikkSupplier,
            Statistikkategori statistikkategori,
            String sykefraværshistorikkLabel
    ) {
        return CompletableFuture
                .supplyAsync(sykefraværshistorikkSupplier)
                .orTimeout(TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER, TimeUnit.SECONDS)
                .exceptionally(
                        e -> {
                            log.warn(
                                    format("Fikk '%s' ved uthenting av sykefravarsstatistikk '%s'. " +
                                                    "Returnerer en tom liste",
                                            e.getMessage(),
                                            statistikkategori
                                    ),
                                    e
                            );
                            return byggSykefraværshistorikk(
                                    statistikkategori,
                                    sykefraværshistorikkLabel,
                                    Collections.EMPTY_LIST
                            );
                        });
    }

    private static KvartalsvisSykefraværshistorikk byggSykefraværshistorikk(
            Statistikkategori statistikkategori,
            String label,
            List<SykefraværForEttKvartal> sykefraværForEttKvartal
    ) {
        KvartalsvisSykefraværshistorikk kvartalsvisSykefraværshistorikk = new KvartalsvisSykefraværshistorikk();
        kvartalsvisSykefraværshistorikk.setType(statistikkategori);
        kvartalsvisSykefraværshistorikk.setLabel(label);
        kvartalsvisSykefraværshistorikk.setSykefraværForEttKvartal(sykefraværForEttKvartal);

        return kvartalsvisSykefraværshistorikk;
    }
}
