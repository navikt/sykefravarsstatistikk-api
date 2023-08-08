package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller;

public enum Statistikkategori {
  LAND,
  SEKTOR,
  NÆRING,
  BRANSJE,
  VIRKSOMHET,
  OVERORDNET_ENHET,
  NÆRING5SIFFER, // deprecated -- bruk NÆRINGSKODE i stedet
  NÆRINGSKODE
}
