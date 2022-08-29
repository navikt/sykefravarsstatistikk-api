package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoDvhDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoerRepository;
import org.springframework.stereotype.Component;

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

    private static Publiseringsdatoer byggPubliseringsdatoInfo(
          List<PubliseringsdatoDvhDto> publiseringsdatoer
    ) {
        log.info("hei fra byggPubliseringsdatoInfo");
        PubliseringsdatoDvhDto forrigePubliseringsdatoElement = finnForrigePubliseringsdatoElement(publiseringsdatoer);
        ÅrstallOgKvartal gjeldendePeriode = finnÅrstallOgKvartalForPeriode(forrigePubliseringsdatoElement.getRapportPeriode());
        return Publiseringsdatoer.builder()
              .gjeldendeÅrstall(String.valueOf(gjeldendePeriode.getÅrstall()))
              .gjeldendeKvartal(String.valueOf(gjeldendePeriode.getKvartal()))
              .forrigePubliseringsdato(forrigePubliseringsdatoElement.getOffentligDato()
                    .toString())
              .nestePubliseringsdato("neste dato")
              .build();
    }

    private static PubliseringsdatoDvhDto finnForrigePubliseringsdatoElement(
          List<PubliseringsdatoDvhDto> publiseringsdatoer
    ) {
        List<PubliseringsdatoDvhDto> tidligerePubliseringsdatoer =
              publiseringsdatoer.stream()
                    .filter(
                          publiseringsdato ->
                                publiseringsdato.getOffentligDato().toLocalDate().isBefore(LocalDate.now())
                    ).sorted(PubliseringsdatoDvhDto::sammenlignOffentligDato)
                    .collect(Collectors.toList());
        return tidligerePubliseringsdatoer.get(tidligerePubliseringsdatoer.size() - 1);
    }

    private static PubliseringsdatoDvhDto finnNestePubliseringsdatoElement(
          List<PubliseringsdatoDvhDto> publiseringsdatoer
    ) {
        List<PubliseringsdatoDvhDto> tidligerePubliseringsdatoer =
              publiseringsdatoer.stream()
                    .filter(
                          publiseringsdato ->
                                publiseringsdato.getOffentligDato().toLocalDate().isAfter(LocalDate.now())
                    ).sorted(PubliseringsdatoDvhDto::sammenlignOffentligDato)
                    .collect(Collectors.toList());
        return tidligerePubliseringsdatoer.get(0);
    }

    private static ÅrstallOgKvartal finnÅrstallOgKvartalForPeriode(
          Integer rapportPeriode
    ) {
        String periode = String.valueOf(rapportPeriode);
        return new ÅrstallOgKvartal(
              Integer.parseInt(periode.substring(0, 4)), Integer.parseInt(periode.substring(4))
        );
    }
}
