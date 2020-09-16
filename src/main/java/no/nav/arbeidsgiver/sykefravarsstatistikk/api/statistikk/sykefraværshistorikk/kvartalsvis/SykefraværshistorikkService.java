package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.sykefravær.KvartalsvisSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret.*;
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
public class SykefraværshistorikkService {

    public static final int TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER = 3;
    public static final int LENGDE_PÅ_NÆRINGSKODE_AV_BRANSJENIVÅ = 5;
    public static final String SYKEFRAVÆRPROSENT_LAND_LABEL = "Norge";

    private final KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final SektorMappingService sektorMappingService;
    private final KlassifikasjonerRepository klassifikasjonerRepository;
    private final Bransjeprogram bransjeprogram;

    public SykefraværshistorikkService(
            KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository,
            EnhetsregisteretClient enhetsregisteretClient,
            SektorMappingService sektorMappingService,
            KlassifikasjonerRepository klassifikasjonerRepository,
            Bransjeprogram bransjeprogram) {
        this.kvartalsvisSykefraværprosentRepository = kvartalsvisSykefraværprosentRepository;
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.sektorMappingService = sektorMappingService;
        this.klassifikasjonerRepository = klassifikasjonerRepository;
        this.bransjeprogram = bransjeprogram;
    }

    public List<KvartalsvisSykefraværshistorik> hentSykefraværshistorikk(
            Underenhet underenhet,
            InstitusjonellSektorkode institusjonellSektorkode
    ) {
        Optional<Bransje> bransje = bransjeprogram.finnBransje(underenhet);
        Sektor ssbSektor = sektorMappingService.mapTilSSBSektorKode(institusjonellSektorkode);

        boolean erIBransjeprogram =
                bransje.isPresent()
                        && bransje.get().lengdePåNæringskoder() == LENGDE_PÅ_NÆRINGSKODE_AV_BRANSJENIVÅ;

        List<KvartalsvisSykefraværshistorik> kvartalsvisSykefraværshistorikListe =
                Stream.of(
                        uthentingMedFeilhåndteringOgTimeout(
                                () -> hentSykefraværshistorikkLand(),
                                SykefraværshistorikkType.LAND,
                                SYKEFRAVÆRPROSENT_LAND_LABEL),
                        uthentingMedFeilhåndteringOgTimeout(
                                () -> hentSykefraværshistorikkSektor(ssbSektor),
                                SykefraværshistorikkType.SEKTOR,
                                ssbSektor.getNavn()),
                        erIBransjeprogram ?
                                uthentingMedFeilhåndteringOgTimeout(
                                        () -> hentSykefraværshistorikkBransje(bransje.get()),
                                        SykefraværshistorikkType.BRANSJE,
                                        bransje.get().getNavn()
                                )
                                : uthentingAvSykefraværshistorikkNæring(underenhet),
                        uthentingMedFeilhåndteringOgTimeout(
                                () -> hentSykefraværshistorikkVirksomhet(underenhet, SykefraværshistorikkType.VIRKSOMHET),
                                SykefraværshistorikkType.VIRKSOMHET,
                                underenhet.getNavn())
                )
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());

        return kvartalsvisSykefraværshistorikListe;
    }

    public List<KvartalsvisSykefraværshistorik> hentSykefraværshistorikk(Underenhet underenhet, OverordnetEnhet overordnetEnhet) {

        KvartalsvisSykefraværshistorik historikkForOverordnetEnhet = uthentingMedFeilhåndteringOgTimeout(
                () -> hentSykefraværshistorikkVirksomhet(overordnetEnhet, SykefraværshistorikkType.OVERORDNET_ENHET),
                SykefraværshistorikkType.OVERORDNET_ENHET,
                underenhet.getNavn()
        ).join();

        List<KvartalsvisSykefraværshistorik> kvartalsvisSykefraværshistorikListe = hentSykefraværshistorikk(
                underenhet,
                overordnetEnhet.getInstitusjonellSektorkode()
        );
        kvartalsvisSykefraværshistorikListe.add(historikkForOverordnetEnhet);

        return kvartalsvisSykefraværshistorikListe;
    }

    private KvartalsvisSykefraværshistorik hentSykefraværshistorikkLand() {
        return byggSykefraværshistorikk(
                SykefraværshistorikkType.LAND,
                SYKEFRAVÆRPROSENT_LAND_LABEL,
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand()
        );
    }

    private KvartalsvisSykefraværshistorik hentSykefraværshistorikkSektor(Sektor ssbSektor) {
        return byggSykefraværshistorikk(
                SykefraværshistorikkType.SEKTOR,
                ssbSektor.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(ssbSektor)
        );
    }

    private KvartalsvisSykefraværshistorik hentSykefraværshistorikkNæring(Næring næring) {
        return byggSykefraværshistorikk(
                SykefraværshistorikkType.NÆRING,
                næring.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentNæring(næring)
        );
    }

    private KvartalsvisSykefraværshistorik hentSykefraværshistorikkBransje(Bransje bransje) {
        return byggSykefraværshistorikk(
                SykefraværshistorikkType.BRANSJE,
                bransje.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentBransje(bransje)
        );
    }

    private KvartalsvisSykefraværshistorik hentSykefraværshistorikkVirksomhet(
            Virksomhet virksomhet,
            SykefraværshistorikkType type
    ) {
        return byggSykefraværshistorikk(
                type,
                virksomhet.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(virksomhet)
        );
    }

    private CompletableFuture<KvartalsvisSykefraværshistorik> uthentingAvSykefraværshistorikkNæring(Underenhet underenhet) {
        Næringskode5Siffer næring5siffer = underenhet.getNæringskode();
        return uthentingMedTimeout(
                () -> klassifikasjonerRepository.hentNæring(næring5siffer.hentNæringskode2Siffer())

        ).thenCompose(
                næring ->
                        uthentingMedFeilhåndteringOgTimeout(
                                () -> hentSykefraværshistorikkNæring(næring),
                                SykefraværshistorikkType.NÆRING,
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
                                SykefraværshistorikkType.NÆRING,
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

    private static CompletableFuture<KvartalsvisSykefraværshistorik> uthentingMedFeilhåndteringOgTimeout(
            Supplier<KvartalsvisSykefraværshistorik> sykefraværshistorikkSupplier,
            SykefraværshistorikkType sykefraværshistorikkType,
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
                                            sykefraværshistorikkType
                                    ),
                                    e
                            );
                            return byggSykefraværshistorikk(
                                    sykefraværshistorikkType,
                                    sykefraværshistorikkLabel,
                                    Collections.EMPTY_LIST
                            );
                        });
    }

    private static KvartalsvisSykefraværshistorik byggSykefraværshistorikk(
            SykefraværshistorikkType sykefraværshistorikkType,
            String label,
            List<KvartalsvisSykefravær> kvartalsvisSykefravær
    ) {
        KvartalsvisSykefraværshistorik kvartalsvisSykefraværshistorik = new KvartalsvisSykefraværshistorik();
        kvartalsvisSykefraværshistorik.setType(sykefraværshistorikkType);
        kvartalsvisSykefraværshistorik.setLabel(label);
        kvartalsvisSykefraværshistorik.setKvartalsvisSykefravær(kvartalsvisSykefravær);

        return kvartalsvisSykefraværshistorik;
    }
}
