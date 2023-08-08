package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.statistikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Sykefraværsstatistikk;

import java.util.List;

public interface SykefraværsstatistikkIntegrasjonUtils {

  DeleteSykefraværsstatistikkFunction getDeleteFunction();

  BatchCreateSykefraværsstatistikkFunction getBatchCreateFunction(
      List<? extends Sykefraværsstatistikk> list);
}
