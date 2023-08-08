package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringMetadataVirksomhetService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringPerStatistikkKategoriService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
@Slf4j
@RequestMapping(value = "eksportering")
@Profile({"local", "dev", "prod", "mvc-test"})
public class EksporteringController {

  private final EksporteringService eksporteringService;
  private final EksporteringPerStatistikkKategoriService eksporteringPerStatistikkKategoriService;
  private final EksporteringMetadataVirksomhetService eksporteringMetadataVirksomhetService;

  public EksporteringController(
      EksporteringService eksporteringService,
      EksporteringPerStatistikkKategoriService eksporteringPerStatistikkKategoriService,
      EksporteringMetadataVirksomhetService eksporteringMetadataVirksomhetService) {
    this.eksporteringService = eksporteringService;
    this.eksporteringPerStatistikkKategoriService = eksporteringPerStatistikkKategoriService;
    this.eksporteringMetadataVirksomhetService = eksporteringMetadataVirksomhetService;
  }

  @PostMapping("/reeksport")
  public ResponseEntity<HttpStatus> reeksportMedKafka(
      @RequestParam int årstall,
      @RequestParam int kvartal) {

    ÅrstallOgKvartal årstallOgKvartal = new ÅrstallOgKvartal(årstall, kvartal);
    int antallEksportert =
        eksporteringService.eksporter(årstallOgKvartal);

    if (antallEksportert >= 0) {
      return ResponseEntity.ok(HttpStatus.CREATED);
    } else {
      return ResponseEntity.ok(HttpStatus.OK);
    }
  }

  @PostMapping("/reeksport/statistikkkategori")
  public ResponseEntity<HttpStatus> reeksportMedKafka(
      @RequestParam int årstall,
      @RequestParam int kvartal,
      @RequestParam Statistikkategori kategori
  ) {
    if (Statistikkategori.LAND != kategori
        && Statistikkategori.VIRKSOMHET != kategori
        && Statistikkategori.NÆRING != kategori
        && Statistikkategori.SEKTOR != kategori
        && Statistikkategori.BRANSJE != kategori
        && Statistikkategori.NÆRINGSKODE != kategori) {
      return ResponseEntity.badRequest().build();
    }

    eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(
        new ÅrstallOgKvartal(årstall, kvartal), kategori);

    return ResponseEntity.ok(HttpStatus.OK);
  }

  @PostMapping("/reeksport/metadata")
  public ResponseEntity<HttpStatus> reeksportMetadata(
      @RequestParam int årstall, @RequestParam int kvartal) {
    ÅrstallOgKvartal årstallOgKvartal = new ÅrstallOgKvartal(årstall, kvartal);
    eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(årstallOgKvartal);

    return ResponseEntity.ok(HttpStatus.OK);
  }

}
