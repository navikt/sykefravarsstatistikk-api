package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class SykefraværsstatistikkIntegrasjon {

    public static final String ARSTALL = "arstall";
    public static final String KVARTAL = "kvartal";
    public static final String TAPTE_DAGSVERK = "tapte_dagsverk";
    public static final String MULIGE_DAGSVERK = "mulige_dagsverk";

    final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public SykefraværsstatistikkIntegrasjon(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }
}
