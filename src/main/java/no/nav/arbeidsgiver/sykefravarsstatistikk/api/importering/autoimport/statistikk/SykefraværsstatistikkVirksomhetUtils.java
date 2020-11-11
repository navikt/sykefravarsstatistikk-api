package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Sykefraværsstatistikk;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

import java.util.Arrays;
import java.util.List;

public class SykefraværsstatistikkVirksomhetUtils extends SykefraværsstatistikkIntegrasjon
        implements SykefraværsstatistikkIntegrasjonUtils {


    public SykefraværsstatistikkVirksomhetUtils(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(namedParameterJdbcTemplate);
    }

    @Override
    public DeleteSykefraværsstatistikkFunction getDeleteFunction() {
        DeleteSykefraværsstatistikkFunction function =
                (ÅrstallOgKvartal årstallOgKvartal) -> {
                    SqlParameterSource namedParameters =
                            new MapSqlParameterSource()
                                    .addValue(ARSTALL, årstallOgKvartal.getÅrstall())
                                    .addValue(KVARTAL, årstallOgKvartal.getKvartal());

                    int antallSlettet =
                            namedParameterJdbcTemplate.update(
                                    String.format(
                                            "delete from sykefravar_statistikk_virksomhet where arstall = :%s and kvartal = :%s",
                                            ARSTALL, KVARTAL),
                                    namedParameters);
                    return antallSlettet;
                };
        return function;
    }

    @Override
    public BatchCreateSykefraværsstatistikkFunction getBatchCreateFunction(
            List<? extends Sykefraværsstatistikk> statistikk
    ) {

        BatchCreateSykefraværsstatistikkFunction function =
                () -> {
                    SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(statistikk.toArray());

                    int[] results = namedParameterJdbcTemplate.batchUpdate(
                            "insert into sykefravar_statistikk_virksomhet " +
                                    "(arstall, kvartal, orgnr, varighet, antall_personer, tapte_dagsverk, mulige_dagsverk, naring_kode)  " +
                                    "values " +
                                    "(:årstall, :kvartal, :orgnr, :varighet, :antallPersoner, :tapteDagsverk, :muligeDagsverk, :næringskode5siffer)",
                            batch);
                    return Arrays.stream(results).sum();
                };

        return function;
    }

}
