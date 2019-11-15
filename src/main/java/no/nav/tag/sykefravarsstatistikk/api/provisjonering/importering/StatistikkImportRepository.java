package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.common.SlettOgOpprettResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.*;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.CreateSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Profile({"local", "dev"})
@Slf4j
@Component
public class StatistikkImportRepository {

  public static final int INSERT_BATCH_STØRRELSE = 1000;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public StatistikkImportRepository(
      @Qualifier("sykefravarsstatistikkJdbcTemplate")
          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }


  public SlettOgOpprettResultat importSykefraværsstatistikkLand(
          List<SykefraværsstatistikkLand> landStatistikk,
          ÅrstallOgKvartal årstallOgKvartal
  ) {

    SykefraværsstatistikkLandUtils sykefraværsstatistikkLandUtils =
        new SykefraværsstatistikkLandUtils(namedParameterJdbcTemplate);

    return importStatistikk(landStatistikk, årstallOgKvartal, sykefraværsstatistikkLandUtils);
  }

  public SlettOgOpprettResultat importSykefraværsstatistikkSektor(
          List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor,
          ÅrstallOgKvartal årstallOgKvartal
  ) {

    SykefraværsstatistikkSektorUtils sykefraværsstatistikkSektorUtils =
            new SykefraværsstatistikkSektorUtils(namedParameterJdbcTemplate);

    return importStatistikk(sykefraværsstatistikkSektor, årstallOgKvartal, sykefraværsstatistikkSektorUtils);
  }

  public SlettOgOpprettResultat importSykefraværsstatistikkNæring(
          List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
          ÅrstallOgKvartal årstallOgKvartal
  ) {
    SykefraværsstatistikkNæringUtils sykefraværsstatistikkNæringUtils =
            new SykefraværsstatistikkNæringUtils(namedParameterJdbcTemplate);

    return importStatistikk(sykefraværsstatistikkNæring, årstallOgKvartal, sykefraværsstatistikkNæringUtils);
  }

  public SlettOgOpprettResultat importSykefraværsstatistikkVirksomhet(
          List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhet,
          ÅrstallOgKvartal årstallOgKvartal
  ) {
    SykefraværsstatistikkVirksomhetUtils sykefraværsstatistikkVirksomhetUtils =
            new SykefraværsstatistikkVirksomhetUtils(namedParameterJdbcTemplate);

    return importStatistikk(sykefraværsstatistikkVirksomhet, årstallOgKvartal, sykefraværsstatistikkVirksomhetUtils);
  }


  SlettOgOpprettResultat importStatistikk(
          List<? extends Sykefraværsstatistikk> sykefraværsstatistikk,
          ÅrstallOgKvartal årstallOgKvartal,
          SykefraværsstatistikkIntegrasjonUtils sykefraværsstatistikkIntegrasjonUtils
  ) {

    if (sykefraværsstatistikk.isEmpty()) {
      return SlettOgOpprettResultat.tomtResultat();
    }

    int antallSletet = slett(årstallOgKvartal, sykefraværsstatistikkIntegrasjonUtils.getDeleteFunction());
    int antallOprettet = batchOpprett(
            sykefraværsstatistikk,
            sykefraværsstatistikkIntegrasjonUtils.getCreateFunction(),
            INSERT_BATCH_STØRRELSE
    );

    return new SlettOgOpprettResultat(antallSletet, antallOprettet);
  }


  private int slett(ÅrstallOgKvartal årstallOgKvartal, DeleteSykefraværsstatistikkFunction deleteFunction) {
      int antallSlettet = deleteFunction.apply(årstallOgKvartal);
      return antallSlettet;
  }

  int batchOpprett(
          List<? extends Sykefraværsstatistikk> sykefraværsstatistikk,
          CreateSykefraværsstatistikkFunction createFunction,
          int insertBatchStørrelse
  ) {
    log.info(
            String.format(
                    "Starter import av sykefraværsstatistikk. Skal importere %d rader",
                    sykefraværsstatistikk.size()
            )
    );
    List<? extends List<? extends Sykefraværsstatistikk>> subsets =
            Lists.partition(sykefraværsstatistikk, insertBatchStørrelse);
    AtomicInteger antallOpprettet = new AtomicInteger();

    subsets.forEach(s -> {
              int opprettet = opprett(s, createFunction);
              int opprettetHittilNå = antallOpprettet.addAndGet(opprettet);

              log.info(String.format("Opprettet %d rader", opprettetHittilNå));
            }
    );

    return antallOpprettet.get();
  }

  private int opprett(
          List<? extends Sykefraværsstatistikk> sykefraværsstatistikk,
          CreateSykefraværsstatistikkFunction createFunction) {

    AtomicInteger antallOpprettet = new AtomicInteger();
    sykefraværsstatistikk.forEach(
        stat -> {
          createFunction.apply(stat);
          antallOpprettet.getAndIncrement();
        });
    return antallOpprettet.get();
  }

}
