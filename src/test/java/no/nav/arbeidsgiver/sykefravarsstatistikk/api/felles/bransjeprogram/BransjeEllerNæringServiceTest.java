package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.NæringOgNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.bransjeprogram.ArbeidsmiljøportalenBransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.ORGNR_VIRKSOMHET_1;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.ORGNR_VIRKSOMHET_2;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class BransjeEllerNæringServiceTest {

  BransjeEllerNæringService bransjeEllerNæringService;

  @Mock private KlassifikasjonerRepository klassifikasjonerRepository;

  private final Næringskode5Siffer barnehage = new Næringskode5Siffer("88911", "Barnehager");
  private VirksomhetMetadata virksomhetMetadata =
      new VirksomhetMetadata(
          ORGNR_VIRKSOMHET_1,
          "Virksomhet 1",
          RECTYPE_FOR_VIRKSOMHET,
          "1",
          "88",
          "88000",
          SISTE_PUBLISERTE_KVARTAL);

  @BeforeEach
  public void setUp() {
    bransjeEllerNæringService =
        new BransjeEllerNæringService(klassifikasjonerRepository);
  }

  @Test
  public void skalHenteDataPåBransjeEllerNæringsnivå_skalReturnereBransje_forBarnehager() {
    BransjeEllerNæring actual = bransjeEllerNæringService.bestemFraNæringskode(barnehage);

    assertThat(actual.isBransje()).isTrue();
  }

  @Test
  public void
      skalHenteDataPåBransjeEllerNæringsnivå_skalReturnereNæring_forBedriftINæringsmiddelindustrien() {
    // En bedrift i næringsmiddelindustrien er i bransjeprogrammet, men data hentes likevel på
    // tosiffernivå, aka næringsnivå
    Næringskode5Siffer næringINæringsmiddelindustriBransjen =
        new Næringskode5Siffer("10411", "Produksjon av rå fiskeoljer og fett");
    BransjeEllerNæring actual =
        bransjeEllerNæringService.bestemFraNæringskode(næringINæringsmiddelindustriBransjen);

    assertThat(actual.isBransje()).isFalse();
  }

  @Test
  public void finnBransejFraMetadata__skalFinneRiktigBransjeFraMetadata() {
    virksomhetMetadata.leggTilNæringOgNæringskode5siffer(
        List.of(
            new NæringOgNæringskode5siffer(barnehage.hentNæringskode2Siffer(), barnehage.getKode()),
            new NæringOgNæringskode5siffer("00", "00000")));
    BransjeEllerNæring resultat =
        bransjeEllerNæringService.finnBransjeFraMetadata(virksomhetMetadata, List.of());
    assertTrue(resultat.isBransje());
    assertThat(resultat.getBransje().getType()).isEqualTo(ArbeidsmiljøportalenBransje.BARNEHAGER);
    assertThat(resultat.getBransje().getKoderSomSpesifisererNæringer()).isEqualTo(List.of("88911"));
  }

  @Test
  public void finnBransejFraMetadata__skalIkkeFeileVedManglendeAvNæringskode5sifferListe() {
    BransjeEllerNæring resultat =
        bransjeEllerNæringService.finnBransjeFraMetadata(virksomhetMetadata, List.of());
    assertFalse(resultat.isBransje());
    assertThat(resultat.getNæring().getKode()).isEqualTo("88");
    assertThat(resultat.getNæring().getNavn()).isEqualTo("Ukjent næring");
  }

  @Test
  public void finnBransejFraMetadata__skalReturnereRiktigNæringsbeskrivelse() {
    VirksomhetMetadata virksomhetMetadata2 =
        new VirksomhetMetadata(
            ORGNR_VIRKSOMHET_2,
            "Virksomhet 2",
            RECTYPE_FOR_VIRKSOMHET,
            "1",
            "11",
            "11000",
            SISTE_PUBLISERTE_KVARTAL);
    BransjeEllerNæring resultat =
        bransjeEllerNæringService.finnBransjeFraMetadata(
            virksomhetMetadata2,
            List.of(
                new Næring("02", "Skogbruk og tjenester tilknyttet skogbruk"),
                new Næring("11", "Produksjon av drikkevarer")));
    assertFalse(resultat.isBransje());
    assertThat(resultat.getNæring().getKode()).isEqualTo("11");
    assertThat(resultat.getNæring().getNavn()).isEqualTo("Produksjon av drikkevarer");
  }
}
