package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.Sykefraværsstatistikk;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.BatchCreateSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

import java.util.Arrays;
import java.util.List;

public class SykefraværsstatistikkNæring5sifferUtils extends SykefraværsstatistikkIntegrasjon
        implements SykefraværsstatistikkIntegrasjonUtils {


    public SykefraværsstatistikkNæring5sifferUtils(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
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
                                            "delete from sykefravar_statistikk_naring5siffer where arstall = :%s and kvartal = :%s",
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
                            "insert into sykefravar_statistikk_naring5siffer " +
                                    "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, mulige_dagsverk) " +
                                    "values " +
                                    "(:årstall, :kvartal, :næringkode, :antallPersoner, :tapteDagsverk, :muligeDagsverk)",
                            batch);
                    return Arrays.stream(results).sum();
                };

        return function;
    }

}
