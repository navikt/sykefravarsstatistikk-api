package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.SlettOgOpprettResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Statistikkilde;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Sykefraværsstatistikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkLand;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæringMedVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetMedGradering;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.SykefraværsstatistikkIntegrasjon.ARSTALL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.SykefraværsstatistikkIntegrasjon.KVARTAL;

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


    public Kvartal hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde type) {
        List<Kvartal> alleKvartaler =
                hentAlleÅrstallOgKvartalForSykefraværsstatistikk(type);
        return alleKvartaler.get(0);
    }

    @NotNull
    public List<Kvartal> hentAlleÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde type) {
        return namedParameterJdbcTemplate.query(
                String.format("select distinct arstall, kvartal " +
                        "from %s " +
                        "order by arstall desc, kvartal desc", type.tabell),
                new MapSqlParameterSource(),
                (resultSet, rowNum) ->
                        new Kvartal(
                                resultSet.getInt("arstall"),
                                resultSet.getInt("kvartal")
                        )
        );
    }


    // IMPORT metoder

    public SlettOgOpprettResultat importSykefraværsstatistikkLand(
            List<SykefraværsstatistikkLand> landStatistikk,
            Kvartal kvartal
    ) {

        SykefraværsstatistikkLandUtils sykefraværsstatistikkLandUtils =
                new SykefraværsstatistikkLandUtils(namedParameterJdbcTemplate);

        return importStatistikk(
                "land",
                landStatistikk,
                kvartal,
                sykefraværsstatistikkLandUtils
        );
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkSektor(
            List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor,
            Kvartal kvartal
    ) {

        SykefraværsstatistikkSektorUtils sykefraværsstatistikkSektorUtils =
                new SykefraværsstatistikkSektorUtils(namedParameterJdbcTemplate);

        return importStatistikk(
                "sektor",
                sykefraværsstatistikkSektor,
                kvartal,
                sykefraværsstatistikkSektorUtils
        );
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkNæring(
            List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
            Kvartal kvartal
    ) {
        SykefraværsstatistikkNæringUtils sykefraværsstatistikkNæringUtils =
                new SykefraværsstatistikkNæringUtils(namedParameterJdbcTemplate);

        return importStatistikk(
                "næring",
                sykefraværsstatistikkNæring,
                kvartal,
                sykefraværsstatistikkNæringUtils
        );
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkNæring5siffer(
            List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
            Kvartal kvartal
    ) {
        SykefraværsstatistikkNæring5sifferUtils sykefraværsstatistikkNæring5sifferUtils =
                new SykefraværsstatistikkNæring5sifferUtils(namedParameterJdbcTemplate);

        return importStatistikk(
                "næring5siffer",
                sykefraværsstatistikkNæring,
                kvartal,
                sykefraværsstatistikkNæring5sifferUtils
        );
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkVirksomhet(
            List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhet,
            Kvartal kvartal
    ) {
        SykefraværsstatistikkVirksomhetUtils sykefraværsstatistikkVirksomhetUtils =
                new SykefraværsstatistikkVirksomhetUtils(namedParameterJdbcTemplate);

        return importStatistikk(
                "virksomhet",
                sykefraværsstatistikkVirksomhet,
                kvartal,
                sykefraværsstatistikkVirksomhetUtils
        );
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkNæringMedVarighet(
            List<SykefraværsstatistikkNæringMedVarighet> sykefraværsstatistikkNæringMedVarighet,
            Kvartal kvartal
    ) {
        if (sykefraværsstatistikkNæringMedVarighet.isEmpty()) {
            loggInfoIngenDataTilImport(kvartal, "næring med varighet");
            return SlettOgOpprettResultat.tomtResultat();
        }

        loggInfoImportStarter(
                sykefraværsstatistikkNæringMedVarighet.size(),
                "næring med varighet",
                kvartal
        );
        int antallSlettet = slettSykefraværsstatistikkNæringMedVarighet(kvartal);
        int antallOprettet = batchOpprettSykefraværsstatistikkNæringMedVarighet(
                sykefraværsstatistikkNæringMedVarighet,
                INSERT_BATCH_STØRRELSE
        );

        return new SlettOgOpprettResultat(antallSlettet, antallOprettet);
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkVirksomhetMedGradering(
            List<SykefraværsstatistikkVirksomhetMedGradering> sykefraværsstatistikkVirksomhetMedGradering,
            Kvartal kvartal
    ) {
        if (sykefraværsstatistikkVirksomhetMedGradering.isEmpty()) {
            loggInfoIngenDataTilImport(kvartal, "virksomhet gradert sykemelding");
            return SlettOgOpprettResultat.tomtResultat();
        }

        loggInfoImportStarter(
                sykefraværsstatistikkVirksomhetMedGradering.size(),
                "virksomhet gradert sykemelding",
                kvartal
        );
        int antallSlettet = slettSykefraværsstatistikkVirksomhetMedGradering(kvartal);
        int antallOprettet = batchOpprettSykefraværsstatistikkVirksomhetMedGradering(
                sykefraværsstatistikkVirksomhetMedGradering,
                INSERT_BATCH_STØRRELSE
        );

        return new SlettOgOpprettResultat(antallSlettet, antallOprettet);

    }


    // Slett metoder

    public int slettSykefraværsstatistikkNæringMedVarighet(Kvartal kvartal) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(ARSTALL, kvartal.getÅrstall())
                        .addValue(KVARTAL, kvartal.getKvartal());

        return namedParameterJdbcTemplate.update(
                String.format(
                        "delete from sykefravar_statistikk_naring_med_varighet where arstall = :%s and kvartal = :%s",
                        ARSTALL, KVARTAL),
                namedParameters);
    }

    public int slettSykefraværsstatistikkVirksomhetMedGradering(Kvartal kvartal) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(ARSTALL, kvartal.getÅrstall())
                        .addValue(KVARTAL, kvartal.getKvartal());

        return namedParameterJdbcTemplate.update(
                String.format(
                        "delete from sykefravar_statistikk_virksomhet_med_gradering where arstall = :%s and kvartal = :%s",
                        ARSTALL, KVARTAL),
                namedParameters);
    }


    // batchOpprett metoder

    public int batchOpprettSykefraværsstatistikkNæringMedVarighet(
            List<? extends Sykefraværsstatistikk> sykefraværsstatistikk,
            int insertBatchStørrelse
    ) {

        List<? extends List<? extends Sykefraværsstatistikk>> subsets =
                Lists.partition(sykefraværsstatistikk, insertBatchStørrelse);

        AtomicInteger antallOpprettet = new AtomicInteger();

        subsets.forEach(subset -> {
                    int opprettet = opprettSykefraværsstatistikkNæringMedVarighet(subset);
                    int opprettetHittilNå = antallOpprettet.addAndGet(opprettet);

                    log.info(String.format("Opprettet %d rader", opprettetHittilNå));
                }
        );

        return antallOpprettet.get();
    }

    public int batchOpprettSykefraværsstatistikkVirksomhetMedGradering(
            List<? extends Sykefraværsstatistikk> sykefraværsstatistikk,
            int insertBatchStørrelse
    ) {

        List<? extends List<? extends Sykefraværsstatistikk>> subsets =
                Lists.partition(sykefraværsstatistikk, insertBatchStørrelse);

        AtomicInteger antallOpprettet = new AtomicInteger();

        subsets.forEach(subset -> {
                    int opprettet = opprettSykefraværsstatistikkVirksomhetMedGradering(subset);
                    int opprettetHittilNå = antallOpprettet.addAndGet(opprettet);

                    log.info(String.format("Opprettet %d rader", opprettetHittilNå));
                }
        );

        return antallOpprettet.get();
    }


    // opprett metoder

    public int opprettSykefraværsstatistikkNæringMedVarighet(
            List<? extends Sykefraværsstatistikk> sykefraværsstatistikk
    ) {
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(sykefraværsstatistikk.toArray());

        int[] results = namedParameterJdbcTemplate.batchUpdate(
                "insert into sykefravar_statistikk_naring_med_varighet " +
                        "(arstall, kvartal, naring_kode, varighet, antall_personer, tapte_dagsverk, mulige_dagsverk) " +
                        "values " +
                        "(:årstall, :kvartal, :næringkode, :varighet, :antallPersoner, :tapteDagsverk, :muligeDagsverk)",
                batch);
        return Arrays.stream(results).sum();
    }

    public int opprettSykefraværsstatistikkVirksomhetMedGradering(
            List<? extends Sykefraværsstatistikk> sykefraværsstatistikk
    ) {
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(sykefraværsstatistikk.toArray());

        int[] results = namedParameterJdbcTemplate.batchUpdate(
                "insert into sykefravar_statistikk_virksomhet_med_gradering " +
                        "(arstall, kvartal, orgnr, naring, naring_kode, rectype, " +
                        "antall_graderte_sykemeldinger, tapte_dagsverk_gradert_sykemelding, " +
                        "antall_sykemeldinger, " +
                        "antall_personer, tapte_dagsverk, mulige_dagsverk) " +
                        "values " +
                        "(:årstall, :kvartal, :orgnr, :næring, :næringkode, :rectype, " +
                        ":antallGraderteSykemeldinger, :tapteDagsverkGradertSykemelding, " +
                        ":antallSykemeldinger, " +
                        ":antallPersoner, :tapteDagsverk, :muligeDagsverk)",
                batch);
        return Arrays.stream(results).sum();
    }

    public SlettOgOpprettResultat importStatistikk(
            String statistikktype,
            List<? extends Sykefraværsstatistikk> sykefraværsstatistikk,
            Kvartal kvartal,
            SykefraværsstatistikkIntegrasjonUtils sykefraværsstatistikkIntegrasjonUtils
    ) {

        if (sykefraværsstatistikk.isEmpty()) {
            log.info(
                    String.format("Ingen sykefraværsstatistikk (%s) til import for årstall '%d' og kvartal '%d'. ",
                            statistikktype,
                            kvartal.getÅrstall(),
                            kvartal.getKvartal()
                    )
            );
            return SlettOgOpprettResultat.tomtResultat();
        }

        log.info(
                String.format(
                        "Starter import av sykefraværsstatistikk (%s) for årstall '%d' og kvartal '%d'. " +
                                "Skal importere %d rader",
                        statistikktype,
                        kvartal.getÅrstall(),
                        kvartal.getKvartal(),
                        sykefraværsstatistikk.size()
                )
        );
        int antallSlettet = slett(kvartal, sykefraværsstatistikkIntegrasjonUtils.getDeleteFunction());
        int antallOprettet = batchOpprett(
                sykefraværsstatistikk,
                sykefraværsstatistikkIntegrasjonUtils,
                INSERT_BATCH_STØRRELSE
        );

        return new SlettOgOpprettResultat(antallSlettet, antallOprettet);
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


    private int slett(Kvartal kvartal, DeleteSykefraværsstatistikkFunction deleteFunction) {
        int antallSlettet = deleteFunction.apply(kvartal);
        return antallSlettet;
    }

    private void loggInfoIngenDataTilImport(Kvartal kvartal, final String beskrivelse) {
        log.info(
                String.format("Ingen sykefraværsstatistikk ('%s') til import for årstall '%d' og kvartal '%d'. ",
                        beskrivelse,
                        kvartal.getÅrstall(),
                        kvartal.getKvartal()
                )
        );
    }

    private void loggInfoImportStarter(int importSize, String beskrivelse, Kvartal kvartal) {
        log.info(
                String.format(
                        "Starter import av sykefraværsstatistikk (%s) for årstall '%d' og kvartal '%d'. " +
                                "Skal importere %d rader",
                        beskrivelse,
                        kvartal.getÅrstall(),
                        kvartal.getKvartal(),
                        importSize
                )
        );
    }
}
