package no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.Sykefraværsstatistikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkVirksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.BatchCreateSykefraværsstatistikkFunction;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
import org.springframework.jdbc.core.namedparam.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
