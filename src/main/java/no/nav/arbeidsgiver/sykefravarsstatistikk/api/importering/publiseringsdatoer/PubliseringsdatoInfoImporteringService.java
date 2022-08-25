package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.publiseringsdatoer;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.StatistikkRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PubliseringsdatoInfoImporteringService {

    private final StatistikkRepository statistikkRepository;
    private final DatavarehusRepository datavarehusRepository;


    public PubliseringsdatoInfoImporteringService(
          StatistikkRepository statistikkRepository,
          DatavarehusRepository datavarehusRepository
    ) {
        this.statistikkRepository = statistikkRepository;
        this.datavarehusRepository = datavarehusRepository;
    }


    public void importerDatoer() {

        List<PubliseringsdatoDbDto> publiseringsdatoerFraDvh =
              datavarehusRepository.hentPubliseringsdatoFullInfo();



    }
}
