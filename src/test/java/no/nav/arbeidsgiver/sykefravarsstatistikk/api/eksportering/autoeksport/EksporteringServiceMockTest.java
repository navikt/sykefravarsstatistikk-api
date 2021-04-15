package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.SykefraværsstatistikkTilEksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EksporteringServiceMockTest {

    @Mock
    private EksporteringRepository eksporteringRepository;
    @Mock
    private VirksomhetMetadataRepository virksomhetMetadataRepository;
    @Mock
    private SykefraværsstatistikkTilEksporteringRepository sykefraværsstatistikkTilEksporteringRepository;
    @Mock
    private KafkaService kafkaService;

    private EksporteringService service;
    private ÅrstallOgKvartal __2020_2 = new ÅrstallOgKvartal(2020, 2);

    
    @BeforeEach
    public void setUp() {
        service = new EksporteringService(
                eksporteringRepository,
                virksomhetMetadataRepository,
                sykefraværsstatistikkTilEksporteringRepository,
                kafkaService,
                true
        );
    }

    @Test
    public void eksporter_returnerer_antall_rader_eksportert() {
        when(eksporteringRepository.hentVirksomhetEksportPerKvartal(__2020_2)).thenReturn(Collections.emptyList());

        int antallEksporterte = service.eksporter(__2020_2);

        Assertions.assertThat(antallEksporterte).isEqualTo(0);
    }
}