package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.importering.integrasjon.utils;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Sykefraværsstatistikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.importering.integrasjon.BatchCreateSykefraværsstatistikkFunction;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
import org.springframework.jdbc.core.namedparam.*;

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
                                    "(arstall, kvartal, orgnr, varighet, antall_personer, tapte_dagsverk, mulige_dagsverk)  " +
                                    "values " +
                                    "(:årstall, :kvartal, :orgnr, :varighet, :antallPersoner, :tapteDagsverk, :muligeDagsverk)",
                            batch);
                    return Arrays.stream(results).sum();
                };

        return function;
    }

}
