package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.ImporttidspunktDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoDbDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoerRepository;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    protected CompletableFuture<Publiseringsdatoer> uthentingMedFeilhåndteringOgTimeout(
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

    private Publiseringsdatoer byggPubliseringsdatoInfo(
          List<PubliseringsdatoDbDto> publiseringsdatoer
    ) {
        final ImporttidspunktDto forrigeImporttidspunktMedPeriode =
              publiseringsdatoerRepository.hentSisteImporttidspunktMedPeriode();

        return Publiseringsdatoer.builder()
              .gjeldendeÅrstall(String.valueOf(forrigeImporttidspunktMedPeriode.getGjeldendeÅrstall()))
              .gjeldendeKvartal(String.valueOf(forrigeImporttidspunktMedPeriode.getGjeldendeKvartal()))
              .forrigePubliseringsdato(
                    forrigeImporttidspunktMedPeriode.getImportertTidspunkt().toLocalDateTime().toLocalDate().toString()
              )
              .nestePubliseringsdato(finnNestePubliseringsdato(publiseringsdatoer).toString())
              .build();
    }

    private LocalDate finnNestePubliseringsdato(
          List<PubliseringsdatoDbDto> publiseringsdatoer
    ) {
        final Timestamp forrigePubliseringsdato =
              publiseringsdatoerRepository.hentSisteImporttidspunktMedPeriode().getImportertTidspunkt();
        List<PubliseringsdatoDbDto> tidligerePubliseringsdatoer =
              publiseringsdatoer.stream()
                    .filter(
                          publiseringsdato ->
                                publiseringsdato.getOffentligDato().toLocalDate().isAfter(forrigePubliseringsdato
                                      .toLocalDateTime().toLocalDate())
                    ).sorted(PubliseringsdatoDbDto::sammenlignOffentligDato)
                    .collect(Collectors.toList());
        return tidligerePubliseringsdatoer.get(0).getOffentligDato().toLocalDate();
    }
}
