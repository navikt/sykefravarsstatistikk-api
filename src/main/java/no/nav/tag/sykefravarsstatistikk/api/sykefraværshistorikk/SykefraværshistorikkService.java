package no.nav.tag.sykefravarsstatistikk.api.sykefraværshistorikk;

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
public class SykefraværshistorikkService {

    public static final int TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER = 3;
    public static final int LENGDE_PÅ_NÆRINGSKODE_AV_BRANSJENIVÅ = 5;
    public static final String SYKEFRAVÆRPROSENT_LAND_LABEL = "Norge";

    private final KvartalsvisSykefraværsprosentRepository kvartalsvisSykefraværprosentRepository;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final SektorMappingService sektorMappingService;
    private final KlassifikasjonerRepository klassifikasjonerRepository;
    private final Bransjeprogram bransjeprogram;

    public SykefraværshistorikkService(
            KvartalsvisSykefraværsprosentRepository kvartalsvisSykefraværprosentRepository,
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


    public List<Sykefraværshistorikk> hentSykefraværshistorikk(Orgnr orgnr) {
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        Enhet enhet = enhetsregisteretClient.hentInformasjonOmEnhet(underenhet.getOverordnetEnhetOrgnr());

        Sektor ssbSektor = sektorMappingService.mapTilSSBSektorKode(enhet.getInstitusjonellSektorkode());
        Optional<Bransje> bransje = bransjeprogram.finnBransje(underenhet);

        boolean erIBransjeprogram =
                bransje.isPresent()
                        && bransje.get().lengdePåNæringskoder() == LENGDE_PÅ_NÆRINGSKODE_AV_BRANSJENIVÅ;

        List<Sykefraværshistorikk> kvartalsvisSykefraværprosentListe =
                Stream.of(
                        uthentingMedFeilhåndteringOgTimeout(
                                () ->
                                        hentSykefraværshistorikkLand(),
                                SykefraværshistorikkType.LAND,
                                SYKEFRAVÆRPROSENT_LAND_LABEL),
                        uthentingMedFeilhåndteringOgTimeout(
                                () ->
                                        hentSykefraværshistorikkSektor(ssbSektor),
                                SykefraværshistorikkType.SEKTOR,
                                ssbSektor.getNavn()),
                        erIBransjeprogram ?
                                uthentingMedFeilhåndteringOgTimeout(
                                        () ->
                                                hentSykefraværshistorikkBransje(bransje.get()),
                                        SykefraværshistorikkType.BRANSJE,
                                        bransje.get().getNavn()
                                )
                                : uthentingAvSykefraværshistorikkNæring(underenhet),
                        uthentingMedFeilhåndteringOgTimeout(
                                () ->
                                        hentSykefraværshistorikkVirksomhet(underenhet),
                                SykefraværshistorikkType.VIRKSOMHET,
                                underenhet.getNavn())
                )
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());

        return kvartalsvisSykefraværprosentListe;
    }


    private Sykefraværshistorikk hentSykefraværshistorikkLand() {
        return byggSykefraværshistorikk(
                SykefraværshistorikkType.LAND,
                SYKEFRAVÆRPROSENT_LAND_LABEL,
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand(SYKEFRAVÆRPROSENT_LAND_LABEL)
        );
    }

    private Sykefraværshistorikk hentSykefraværshistorikkSektor(Sektor ssbSektor) {
        return byggSykefraværshistorikk(
                SykefraværshistorikkType.SEKTOR,
                ssbSektor.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(ssbSektor)
        );
    }

    private Sykefraværshistorikk hentSykefraværshistorikkNæring(Næring næring) {
        return byggSykefraværshistorikk(
                SykefraværshistorikkType.NÆRING,
                næring.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentNæring(næring)
        );
    }

    private Sykefraværshistorikk hentSykefraværshistorikkBransje(Bransje bransje) {
        return byggSykefraværshistorikk(
                SykefraværshistorikkType.BRANSJE,
                bransje.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentBransje(bransje)
        );
    }

    private Sykefraværshistorikk hentSykefraværshistorikkVirksomhet(
            Underenhet underenhet
    ) {
        return byggSykefraværshistorikk(
                SykefraværshistorikkType.VIRKSOMHET,
                underenhet.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(underenhet)
        );
    }

    private CompletableFuture<Sykefraværshistorikk> uthentingAvSykefraværshistorikkNæring(Underenhet underenhet) {
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

    private static CompletableFuture<Sykefraværshistorikk> uthentingMedFeilhåndteringOgTimeout(
            Supplier<Sykefraværshistorikk> sykefraværshistorikkSupplier,
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

    private static Sykefraværshistorikk byggSykefraværshistorikk(
            SykefraværshistorikkType sykefraværshistorikkType,
            String label,
            List<KvartalsvisSykefraværsprosent> kvartalsvisSykefraværsprosent
    ) {
        Sykefraværshistorikk sykefraværshistorikk = new Sykefraværshistorikk();
        sykefraværshistorikk.setType(sykefraværshistorikkType);
        sykefraværshistorikk.setLabel(label);
        sykefraværshistorikk.setKvartalsvisSykefraværsprosent(kvartalsvisSykefraværsprosent);

        return sykefraværshistorikk;
    }
}
