package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import io.vavr.control.Option;
import io.vavr.control.Try;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoDbDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoerRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PubliseringsdatoerService {

  private final PubliseringsdatoerRepository publiseringsdatoerRepository;

  public PubliseringsdatoerService(
      PubliseringsdatoerRepository publiseringsdatoerRepository
  ) {
    this.publiseringsdatoerRepository = publiseringsdatoerRepository;
  }

  public Option<Publiseringsdatoer> hentPubliseringsdatoer() {

    List<PubliseringsdatoDbDto> publiseringsdatoer
        = publiseringsdatoerRepository.hentPubliseringsdatoer();

    return publiseringsdatoerRepository.hentSisteImporttidspunkt()
        .map(
            forrigeImport -> Publiseringsdatoer.builder()
                .gjeldendePeriode(forrigeImport.getGjeldendePeriode())
                .sistePubliseringsdato(forrigeImport.getImportertDato().toString())
                .nestePubliseringsdato(
                    finnNestePubliseringsdato(publiseringsdatoer, forrigeImport.getImportertDato())
                        .map(LocalDate::toString)
                        .getOrElse("Ingen kommende publiseringsdatoer er tilgjengelige"))
                .build()
        );
  }

  private Option<LocalDate> finnNestePubliseringsdato(
      List<PubliseringsdatoDbDto> publiseringsdatoer,
      LocalDate forrigeImporttidspunkt
  ) {

    List<PubliseringsdatoDbDto> fremtidigePubliseringsdatoer = sorterEldsteDatoerFørst(
        filtrerBortDatoerEldreEnnForrigeLanseringsdato(
            publiseringsdatoer,
            forrigeImporttidspunkt
        ));

    return Try.of(() -> fremtidigePubliseringsdatoer.get(0).getOffentligDato().toLocalDate())
        .onFailure(e -> log.warn("Ingen senere publiseringsdatoer er tilgjengelige i kalenderen"))
        .toOption();
  }

  private List<PubliseringsdatoDbDto> filtrerBortDatoerEldreEnnForrigeLanseringsdato(
      List<PubliseringsdatoDbDto> publiseringsdatoer,
      LocalDate forrigePubliseringsdato) {
    return publiseringsdatoer.stream()
        .filter(
            publiseringsdato ->
                publiseringsdato.getOffentligDato().toLocalDate()
                    .isAfter(forrigePubliseringsdato))
        .collect(Collectors.toList());
  }

  private static List<PubliseringsdatoDbDto> sorterEldsteDatoerFørst(
      List<PubliseringsdatoDbDto> datoer) {
    return datoer
        .stream()
        .sorted(PubliseringsdatoDbDto::sammenlignPubliseringsdatoer)
        .collect(Collectors.toList());
  }
}
