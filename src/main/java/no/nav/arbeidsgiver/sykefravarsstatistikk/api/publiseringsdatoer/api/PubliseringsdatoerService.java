package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoDbDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api.Publiseringsdatoer;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.lang.String.format;

@Slf4j
@Component
public class PubliseringsdatoerService {

    public static final int TIMEOUT_UTHENTING_FRA_DB_I_SEKUNDER = 3;

    private final PubliseringsdatoerRepository publiseringsdatoerRepository;

    public PubliseringsdatoerService(
          PubliseringsdatoerRepository publiseringsdatoerRepository
    ) {
        this.publiseringsdatoerRepository = publiseringsdatoerRepository;
    }

    public Publiseringsdatoer hentPubliseringsdatoer() {
        Publiseringsdatoer publiseringsdatoerListe = uthentingMedFeilhåndteringOgTimeout(
                () -> byggPubliseringsdatoInfo(publiseringsdatoerRepository.hentPubliseringsdatoer())
        ).join();

        return publiseringsdatoerListe;
    }

    protected static CompletableFuture<Publiseringsdatoer> uthentingMedFeilhåndteringOgTimeout(
            Supplier<Publiseringsdatoer> publiseringsdatoInfoSupplier
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

    private static Publiseringsdatoer byggPubliseringsdatoInfo (
            List<PubliseringsdatoDbDto> publiseringsdatoDbDto
    ) {
        Publiseringsdatoer publiseringsdatoer = new Publiseringsdatoer();
        publiseringsdatoer.setGjeldendePeriode("periode");
        publiseringsdatoer.setNestePubliseringsdato("neste dato");
        publiseringsdatoer.setForrigePubliseringsdato("forrige dato");

        return publiseringsdatoer;
    }
}
