package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.statistikk;

@FunctionalInterface
public interface BatchCreateSykefraværsstatistikkFunction {
  int apply();
}
