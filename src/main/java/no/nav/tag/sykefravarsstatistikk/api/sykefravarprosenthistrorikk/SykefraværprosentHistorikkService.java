package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransjeprogram;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.KlassifikasjonerRepository;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.SektorMappingService;
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
public class SykefraværprosentHistorikkService {

    public static final int TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER = 3;
    public static final int LENGDE_PÅ_NÆRINGSKODE_AV_BRANSJENIVÅ = 5;
    public static final String SYKEFRAVÆRPROSENT_LAND_LABEL = "Norge";

    private final KvartalsvisSykefraværprosentRepository kvartalsvisSykefraværprosentRepository;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final SektorMappingService sektorMappingService;
    private final KlassifikasjonerRepository klassifikasjonerRepository;
    private final Bransjeprogram bransjeprogram;

    public SykefraværprosentHistorikkService(
            KvartalsvisSykefraværprosentRepository kvartalsvisSykefraværprosentRepository,
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


    public List<KvartalsvisSykefraværprosentHistorikk> hentKvartalsvisSykefraværprosentHistorikk(Orgnr orgnr) {
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        Enhet enhet = enhetsregisteretClient.hentInformasjonOmEnhet(underenhet.getOverordnetEnhetOrgnr());

        Sektor ssbSektor = sektorMappingService.mapTilSSBSektorKode(enhet.getInstitusjonellSektorkode());
        Optional<Bransje> bransje = bransjeprogram.finnBransje(underenhet);

        List<KvartalsvisSykefraværprosentHistorikk> kvartalsvisSykefraværprosentListe =
                Stream.of(
                        feilHåndtertOgTidsbegrensetUthenting(
                                () ->
                                        hentKvartalsvisSykefraværprosentHistorikkLand(),
                                SykefraværsstatistikkType.LAND,
                                SYKEFRAVÆRPROSENT_LAND_LABEL),
                        feilHåndtertOgTidsbegrensetUthenting(
                                () ->
                                        hentKvartalsvisSykefraværprosentHistorikkSektor(ssbSektor),
                                SykefraværsstatistikkType.SEKTOR,
                                ssbSektor.getNavn()),
                        hentHistoriskkForNæringEllerBransje(underenhet, bransje),
                        feilHåndtertOgTidsbegrensetUthenting(
                                () ->
                                        hentKvartalsvissSykefraværprosentHistorikkVirksomhet(underenhet),
                                SykefraværsstatistikkType.VIRKSOMHET,
                                underenhet.getNavn())
                )
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        return kvartalsvisSykefraværprosentListe;
    }


    private KvartalsvisSykefraværprosentHistorikk hentKvartalsvisSykefraværprosentHistorikkLand() {
        return byggKvartalsvisSykefraværprosentHistorikk(
                SykefraværsstatistikkType.LAND,
                SYKEFRAVÆRPROSENT_LAND_LABEL,
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand(SYKEFRAVÆRPROSENT_LAND_LABEL)
        );
    }

    private KvartalsvisSykefraværprosentHistorikk hentKvartalsvisSykefraværprosentHistorikkSektor(Sektor ssbSektor) {
        return byggKvartalsvisSykefraværprosentHistorikk(
                SykefraværsstatistikkType.SEKTOR,
                ssbSektor.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(ssbSektor)
        );
    }

    private KvartalsvisSykefraværprosentHistorikk hentKvartalsvisSykefraværprosentHistorikkNæring(Næring næring) {
        return byggKvartalsvisSykefraværprosentHistorikk(
                SykefraværsstatistikkType.NÆRING,
                næring.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentNæring(næring)
        );
    }

    private KvartalsvisSykefraværprosentHistorikk hentKvartalsvisSykefraværprosentHistorikkBransje(Bransje bransje) {
        return byggKvartalsvisSykefraværprosentHistorikk(
                SykefraværsstatistikkType.BRANSJE,
                bransje.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentBransje(bransje)
        );
    }

    private KvartalsvisSykefraværprosentHistorikk hentKvartalsvissSykefraværprosentHistorikkVirksomhet(
            Underenhet underenhet
    ) {
        return byggKvartalsvisSykefraværprosentHistorikk(
                SykefraværsstatistikkType.VIRKSOMHET,
                underenhet.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(underenhet)
        );
    }

    private CompletableFuture<KvartalsvisSykefraværprosentHistorikk> hentHistoriskkForNæringEllerBransje(
            Underenhet underenhet, Optional<Bransje> bransje
    ) {
        CompletableFuture<KvartalsvisSykefraværprosentHistorikk> hentNæringEllerBransjeFuture;

        if (bransje.isPresent() && bransje.get().lengdePåNæringskoder() == LENGDE_PÅ_NÆRINGSKODE_AV_BRANSJENIVÅ) {
            hentNæringEllerBransjeFuture =
                    feilHåndtertOgTidsbegrensetUthenting(
                            () ->
                                    hentKvartalsvisSykefraværprosentHistorikkBransje(bransje.get()),
                            SykefraværsstatistikkType.BRANSJE,
                            bransje.get().getNavn()
                    );
        } else {
            Næringskode5Siffer næring5siffer = underenhet.getNæringskode();
            hentNæringEllerBransjeFuture =
                    tidsbegrensetUthenting(
                            () -> klassifikasjonerRepository.hentNæring(næring5siffer.hentNæringskode2Siffer())

                    ).thenCompose(
                            næring ->
                                    feilHåndtertOgTidsbegrensetUthenting(
                                            () -> hentKvartalsvisSykefraværprosentHistorikkNæring(næring),
                                            SykefraværsstatistikkType.NÆRING,
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
                                    return byggKvartalsvisSykefraværprosentHistorikk(
                                            SykefraværsstatistikkType.NÆRING,
                                            null,
                                            Collections.EMPTY_LIST
                                    );
                                }
                            }

                    );
        }
        return hentNæringEllerBransjeFuture;
    }

    private static CompletableFuture<Næring> tidsbegrensetUthenting(Supplier<Næring> hentNæringSupplier) {
        return CompletableFuture
                .supplyAsync(hentNæringSupplier)
                .orTimeout(TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER, TimeUnit.SECONDS);
    }

    private static CompletableFuture<KvartalsvisSykefraværprosentHistorikk> feilHåndtertOgTidsbegrensetUthenting(
            Supplier<KvartalsvisSykefraværprosentHistorikk> kvartalsvisSykefraværprosentHistorikkSupplier,
            SykefraværsstatistikkType sykefraværsstatistikkType,
            String sykefraværprosentHistorikkLabel
    ) {
        return CompletableFuture
                .supplyAsync(kvartalsvisSykefraværprosentHistorikkSupplier)
                .orTimeout(TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER, TimeUnit.SECONDS)
                .exceptionally(
                        e -> {
                            log.warn(
                                    format("Fikk '%s' ved uthenting av sykefravarsstatistikk '%s'. " +
                                                    "Returnerer en tom liste",
                                            e.getMessage(),
                                            sykefraværsstatistikkType
                                    ),
                                    e
                            );
                            return byggKvartalsvisSykefraværprosentHistorikk(
                                    sykefraværsstatistikkType,
                                    sykefraværprosentHistorikkLabel,
                                    Collections.EMPTY_LIST
                            );
                        });
    }

    private static KvartalsvisSykefraværprosentHistorikk byggKvartalsvisSykefraværprosentHistorikk(
            SykefraværsstatistikkType sykefraværsstatistikkType,
            String label,
            List<KvartalsvisSykefraværprosent> kvartalsvisSykefraværProsent
    ) {
        KvartalsvisSykefraværprosentHistorikk kvartalsvisSykefraværprosentHistorikk = new KvartalsvisSykefraværprosentHistorikk();
        kvartalsvisSykefraværprosentHistorikk.setSykefraværsstatistikkType(sykefraværsstatistikkType);
        kvartalsvisSykefraværprosentHistorikk.setLabel(label);
        kvartalsvisSykefraværprosentHistorikk.setKvartalsvisSykefraværProsent(kvartalsvisSykefraværProsent);

        return kvartalsvisSykefraværprosentHistorikk;
    }
}
