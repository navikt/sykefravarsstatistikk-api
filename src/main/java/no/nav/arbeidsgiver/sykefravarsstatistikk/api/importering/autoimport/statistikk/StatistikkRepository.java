package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.SlettOgOpprettResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class StatistikkRepository {

    public static final int INSERT_BATCH_STØRRELSE = 10000;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public StatistikkRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate")
                    NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public ÅrstallOgKvartal hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde type) {
        List<ÅrstallOgKvartal> alleÅrstallOgKvartal = namedParameterJdbcTemplate.query(
                String.format("select distinct arstall, kvartal " +
                        "from %s " +
                        "order by arstall desc, kvartal desc", type.tabell),
                new MapSqlParameterSource(),
                (resultSet, rowNum) ->
                        new ÅrstallOgKvartal(
                                resultSet.getInt("arstall"),
                                resultSet.getInt("kvartal")
                        )
        );
        return alleÅrstallOgKvartal.get(0);
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkLand(
            List<SykefraværsstatistikkLand> landStatistikk,
            ÅrstallOgKvartal årstallOgKvartal
    ) {

        SykefraværsstatistikkLandUtils sykefraværsstatistikkLandUtils =
                new SykefraværsstatistikkLandUtils(namedParameterJdbcTemplate);

        return importStatistikk(
                "land",
                landStatistikk,
                årstallOgKvartal,
                sykefraværsstatistikkLandUtils
        );
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkSektor(
            List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor,
            ÅrstallOgKvartal årstallOgKvartal
    ) {

        SykefraværsstatistikkSektorUtils sykefraværsstatistikkSektorUtils =
                new SykefraværsstatistikkSektorUtils(namedParameterJdbcTemplate);

        return importStatistikk(
                "sektor",
                sykefraværsstatistikkSektor,
                årstallOgKvartal,
                sykefraværsstatistikkSektorUtils
        );
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkNæring(
            List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
            ÅrstallOgKvartal årstallOgKvartal
    ) {
        SykefraværsstatistikkNæringUtils sykefraværsstatistikkNæringUtils =
                new SykefraværsstatistikkNæringUtils(namedParameterJdbcTemplate);

        return importStatistikk(
                "næring",
                sykefraværsstatistikkNæring,
                årstallOgKvartal,
                sykefraværsstatistikkNæringUtils
        );
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkNæring5siffer(
            List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
            ÅrstallOgKvartal årstallOgKvartal
    ) {
        SykefraværsstatistikkNæring5sifferUtils sykefraværsstatistikkNæring5sifferUtils =
                new SykefraværsstatistikkNæring5sifferUtils(namedParameterJdbcTemplate);

        return importStatistikk(
                "næring5siffer",
                sykefraværsstatistikkNæring,
                årstallOgKvartal,
                sykefraværsstatistikkNæring5sifferUtils
        );
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkVirksomhet(
            List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhet,
            ÅrstallOgKvartal årstallOgKvartal
    ) {
        SykefraværsstatistikkVirksomhetUtils sykefraværsstatistikkVirksomhetUtils =
                new SykefraværsstatistikkVirksomhetUtils(namedParameterJdbcTemplate);

        return importStatistikk(
                "virksomhet",
                sykefraværsstatistikkVirksomhet,
                årstallOgKvartal,
                sykefraværsstatistikkVirksomhetUtils
        );
    }


    public SlettOgOpprettResultat importStatistikk(
            String statistikktype,
            List<? extends Sykefraværsstatistikk> sykefraværsstatistikk,
            ÅrstallOgKvartal årstallOgKvartal,
            SykefraværsstatistikkIntegrasjonUtils sykefraværsstatistikkIntegrasjonUtils
    ) {

        if (sykefraværsstatistikk.isEmpty()) {
            log.info(
                    String.format("Ingen sykefraværsstatistikk (%s) til import for årstall '%d' og kvartal '%d'. ",
                            statistikktype,
                            årstallOgKvartal.getÅrstall(),
                            årstallOgKvartal.getKvartal()
                    )
            );
            return SlettOgOpprettResultat.tomtResultat();
        }

        log.info(
                String.format(
                        "Starter import av sykefraværsstatistikk (%s) for årstall '%d' og kvartal '%d'. " +
                                "Skal importere %d rader",
                        statistikktype,
                        årstallOgKvartal.getÅrstall(),
                        årstallOgKvartal.getKvartal(),
                        sykefraværsstatistikk.size()
                )
        );
        int antallSletet = slett(årstallOgKvartal, sykefraværsstatistikkIntegrasjonUtils.getDeleteFunction());
        int antallOprettet = batchOpprett(
                sykefraværsstatistikk,
                sykefraværsstatistikkIntegrasjonUtils,
                INSERT_BATCH_STØRRELSE
        );

        return new SlettOgOpprettResultat(antallSletet, antallOprettet);
    }


    private int slett(ÅrstallOgKvartal årstallOgKvartal, DeleteSykefraværsstatistikkFunction deleteFunction) {
        int antallSlettet = deleteFunction.apply(årstallOgKvartal);
        return antallSlettet;
    }

    public int batchOpprett(
            List<? extends Sykefraværsstatistikk> sykefraværsstatistikk,
            SykefraværsstatistikkIntegrasjonUtils utils,
            int insertBatchStørrelse
    ) {

        List<? extends List<? extends Sykefraværsstatistikk>> subsets =
                Lists.partition(sykefraværsstatistikk, insertBatchStørrelse);

        AtomicInteger antallOpprettet = new AtomicInteger();

        subsets.forEach(subset -> {
                    int opprettet = utils.getBatchCreateFunction(subset).apply();
                    int opprettetHittilNå = antallOpprettet.addAndGet(opprettet);

                    log.info(String.format("Opprettet %d rader", opprettetHittilNå));
                }
        );

        return antallOpprettet.get();
    }
}
