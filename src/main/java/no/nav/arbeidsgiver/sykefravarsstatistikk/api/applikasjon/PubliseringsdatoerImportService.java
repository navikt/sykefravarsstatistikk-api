package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.PubliseringsdatoDbDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class PubliseringsdatoerImportService {

  private final PubliseringsdatoerRepository publiseringsdatoerRepository;
  private final DatavarehusRepository datavarehusRepository;

  public PubliseringsdatoerImportService(
      PubliseringsdatoerRepository publiseringsdatoerRepository,
      DatavarehusRepository datavarehusRepository) {
    this.publiseringsdatoerRepository = publiseringsdatoerRepository;
    this.datavarehusRepository = datavarehusRepository;
  }

  public void importerDatoerFraDatavarehus() {
    List<PubliseringsdatoDbDto> publiseringsdatoerFraDvh =
        datavarehusRepository.hentPubliseringsdatoerFraDvh();

    publiseringsdatoerRepository.oppdaterPubliseringsdatoer(publiseringsdatoerFraDvh);
  }
}
