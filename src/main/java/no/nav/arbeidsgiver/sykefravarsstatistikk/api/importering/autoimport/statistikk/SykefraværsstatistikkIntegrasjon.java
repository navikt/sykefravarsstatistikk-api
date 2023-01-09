package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class SykefraværsstatistikkIntegrasjon {

  public static final String ARSTALL = "arstall";
  public static final String KVARTAL = "kvartal";

  final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public SykefraværsstatistikkIntegrasjon(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }
}
