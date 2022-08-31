package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.ImporttidspunktDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoDbDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.PubliseringsdatoerRepository;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PubliseringsdatoerService {

    private final PubliseringsdatoerRepository publiseringsdatoerRepository;

    public PubliseringsdatoerService(
          PubliseringsdatoerRepository publiseringsdatoerRepository
    ) {
        this.publiseringsdatoerRepository = publiseringsdatoerRepository;
    }

    public Publiseringsdatoer hentPubliseringsdatoer() {
        final ImporttidspunktDto forrigeImporttidspunktMedPeriode =
              publiseringsdatoerRepository.hentSisteImporttidspunktMedPeriode();

        return Publiseringsdatoer.builder()
              .gjeldendeÅrstall(String.valueOf(forrigeImporttidspunktMedPeriode.getGjeldendeÅrstall()))
              .gjeldendeKvartal(String.valueOf(forrigeImporttidspunktMedPeriode.getGjeldendeKvartal()))
              .forrigePubliseringsdato(
                    forrigeImporttidspunktMedPeriode.getImportertTidspunkt().toLocalDateTime().toLocalDate().toString()
              )
              .nestePubliseringsdato(
                    finnNestePubliseringsdato(publiseringsdatoerRepository.hentPubliseringsdatoer()).toString()
              ).build();
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
