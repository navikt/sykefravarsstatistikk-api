package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoinfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.publiseringsdatoer.PubliseringsdatoDbDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.lang.String.format;

@Slf4j
@Component
public class PubliseringsdatoInfoService {

    public static final int TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER = 3;

    private final PubliseringsdatoInfoRepository publiseringsdatoInfoRepository;

    public PubliseringsdatoInfoService(
            PubliseringsdatoInfoRepository publiseringsdatoInfoRepository) {
        this.publiseringsdatoInfoRepository = publiseringsdatoInfoRepository;
    }

    public PubliseringsdatoInfo hentPubliseringsdatoInfo() {
        PubliseringsdatoInfo publiseringsdatoInfoListe = uthentingMedFeilhåndteringOgTimeout(
                () -> byggPubliseringsdatoInfo(publiseringsdatoInfoRepository.hentPubliseringsdatoFullInfo())
        ).join();

        return publiseringsdatoInfoListe;
    }

    private static CompletableFuture<Næring> uthentingMedTimeout(Supplier<Næring> hentNæringSupplier) {
        return CompletableFuture
                .supplyAsync(hentNæringSupplier)
                .orTimeout(TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER, TimeUnit.SECONDS);
    }

    protected static CompletableFuture<PubliseringsdatoInfo> uthentingMedFeilhåndteringOgTimeout(
            Supplier<PubliseringsdatoInfo> publiseringsdatoInfoSupplier
    ) {
        return CompletableFuture
                .supplyAsync(publiseringsdatoInfoSupplier)
                .orTimeout(TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER, TimeUnit.SECONDS)
                .exceptionally(
                        e -> {
                            log.warn(
                                    format("Fikk '%s' ved uthenting av publiseringsdatoinfo '%s'. " +
                                                    "Returnerer en tom liste",
                                            e.getMessage()
                                    ),
                                    e
                            );
                            return byggPubliseringsdatoInfo(
                                    Collections.emptyList()
                            );
                        });
    }

    private static PubliseringsdatoInfo byggPubliseringsdatoInfo (
            List<PubliseringsdatoDbDto> publiseringsdatoFullInfo
    ) {
        PubliseringsdatoInfo publiseringsdatoInfo = new PubliseringsdatoInfo();
        publiseringsdatoInfo.setGjeldendePeriode("periode");
        publiseringsdatoInfo.setNestePubliseringsDato("neste dato");
        publiseringsdatoInfo.setForrigePubliseringsDato("forrige dato");

        return publiseringsdatoInfo;
    }
}
