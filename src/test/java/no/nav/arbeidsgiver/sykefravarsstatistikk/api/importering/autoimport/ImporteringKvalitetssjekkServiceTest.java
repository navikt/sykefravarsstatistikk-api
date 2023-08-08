package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.ImporteringKvalitetssjekkService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.ÅrstallOgKvartal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ImporteringKvalitetssjekkServiceTest {

  private ImporteringKvalitetssjekkService.Rådata rådataVarighet;
  private ImporteringKvalitetssjekkService.Rådata rådataGradert;
  private ImporteringKvalitetssjekkService.Rådata rådataForrskjell;

  @BeforeEach
  void setUp() {
    rådataVarighet =
        new ImporteringKvalitetssjekkService.Rådata(
            new ÅrstallOgKvartal(2019, 1),
            2906119,
            new BigDecimal(7605068.452380),
            new BigDecimal(138977027.572757));
    rådataGradert =
        new ImporteringKvalitetssjekkService.Rådata(
            new ÅrstallOgKvartal(2019, 1),
            2906119,
            new BigDecimal(7605068.452380),
            new BigDecimal(138977027.572757));
    rådataForrskjell =
        new ImporteringKvalitetssjekkService.Rådata(
            new ÅrstallOgKvartal(2019, 1),
            3652652,
            new BigDecimal(32659852.654987),
            new BigDecimal(3265698.639654));
  }

  @AfterEach
  void tearDown() {}

  @Test
  void sykefraværRådataErLike_skal_returnere_true_hvis_de_er_like() {

    assertTrue(
        ImporteringKvalitetssjekkService.sykefraværRådataErLike(rådataVarighet, rådataGradert));

    assertTrue(
        ImporteringKvalitetssjekkService.sykefraværRådataErLike(rådataGradert, rådataVarighet));
    assertTrue(ImporteringKvalitetssjekkService.sykefraværRådataErLike(null, null));
  }

  @Test
  void sykefraværRådateErLike_skal_returnere_false_hvis_data_ikke_er_like() {
    assertFalse(ImporteringKvalitetssjekkService.sykefraværRådataErLike(null, rådataVarighet));
    assertFalse(
        ImporteringKvalitetssjekkService.sykefraværRådataErLike(rådataGradert, rådataForrskjell));
  }
}
