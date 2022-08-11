package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class BransjeEllerNæringServiceTest {

  BransjeEllerNæringService bransjeEllerNæringService;

  @Mock private KlassifikasjonerRepository klassifikasjonerRepository;

  @BeforeEach
  public void setUp() {
    bransjeEllerNæringService =
        new BransjeEllerNæringService(new Bransjeprogram(), klassifikasjonerRepository);
  }

  @Test
  public void skalHenteDataPåBransjeEllerNæringsnivå_skalReturnereBransje_forBarnehager() {
    Næringskode5Siffer barnehage = new Næringskode5Siffer("88911", "Barnehager");
    BransjeEllerNæring actual = bransjeEllerNæringService.bestemFraNæringskode(barnehage);

    assertThat(actual.isBransje()).isTrue();
  }

  @Test
  public void skalHenteDataPåBransjeEllerNæringsnivå_skalReturnereNæring_forBedriftINæringsmiddelindustrien() {
    // En bedrift i næringsmiddelindustrien er i bransjeprogrammet, men data hentes likevel på tosiffernivå, aka næringsnivå
    Næringskode5Siffer næringINæringsmiddelindustriBransjen = new Næringskode5Siffer("10411", "Produksjon av rå fiskeoljer og fett");
    BransjeEllerNæring actual = bransjeEllerNæringService.bestemFraNæringskode(næringINæringsmiddelindustriBransjen);

    assertThat(actual.isBransje()).isFalse();
  }
}
