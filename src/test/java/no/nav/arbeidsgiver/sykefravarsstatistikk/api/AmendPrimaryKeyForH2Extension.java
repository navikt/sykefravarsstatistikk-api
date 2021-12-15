package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.setAutoincrementPrimaryKeyForH2Db;

public class AmendPrimaryKeyForH2Extension implements BeforeAllCallback {


    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate =
                SpringExtension.getApplicationContext(extensionContext).getBean(NamedParameterJdbcTemplate.class);
        setAutoincrementPrimaryKeyForH2Db(namedParameterJdbcTemplate, "sykefravar_statistikk_land");
        setAutoincrementPrimaryKeyForH2Db(namedParameterJdbcTemplate, "sykefravar_statistikk_naring");
        setAutoincrementPrimaryKeyForH2Db(namedParameterJdbcTemplate, "sykefravar_statistikk_naring5siffer");
        setAutoincrementPrimaryKeyForH2Db(namedParameterJdbcTemplate, "sykefravar_statistikk_naring_med_varighet");
        setAutoincrementPrimaryKeyForH2Db(namedParameterJdbcTemplate, "sykefravar_statistikk_sektor");
        setAutoincrementPrimaryKeyForH2Db(namedParameterJdbcTemplate, "sykefravar_statistikk_virksomhet");
        setAutoincrementPrimaryKeyForH2Db(namedParameterJdbcTemplate, "sykefravar_statistikk_virksomhet_med_gradering");
        setAutoincrementPrimaryKeyForH2Db(namedParameterJdbcTemplate, "virksomhet_metadata");
        setAutoincrementPrimaryKeyForH2Db(namedParameterJdbcTemplate, "virksomhet_metadata_naring_kode_5siffer");
    }
}
