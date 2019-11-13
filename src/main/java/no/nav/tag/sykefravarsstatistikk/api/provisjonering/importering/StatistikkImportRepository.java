package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering;

import no.nav.tag.sykefravarsstatistikk.api.common.SlettOgOpprettResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.*;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.CreateSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils.SykefraværsstatistikkLandUtils;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils.SykefraværsstatistikkIntegrasjonUtils;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils.SykefraværsstatistikkNæringUtils;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils.SykefraværsstatistikkSektorUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Profile({"local", "dev"})
@Component
public class StatistikkImportRepository {

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

    return importStats(landStatistikk, årstallOgKvartal, sykefraværsstatistikkLandUtils);
  }

  public SlettOgOpprettResultat importSykefraværsstatistikkSektor(
          List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor,
          ÅrstallOgKvartal årstallOgKvartal
  ) {

    SykefraværsstatistikkSektorUtils sykefraværsstatistikkSektorUtils =
            new SykefraværsstatistikkSektorUtils(namedParameterJdbcTemplate);

    return importStats(sykefraværsstatistikkSektor, årstallOgKvartal, sykefraværsstatistikkSektorUtils);
  }

  public SlettOgOpprettResultat importSykefraværsstatistikkNæring(
          List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
          ÅrstallOgKvartal årstallOgKvartal
  ) {
    SykefraværsstatistikkNæringUtils sykefraværsstatistikkNæringUtils =
            new SykefraværsstatistikkNæringUtils(namedParameterJdbcTemplate);

    return importStats(sykefraværsstatistikkNæring, årstallOgKvartal, sykefraværsstatistikkNæringUtils);
  }


  private SlettOgOpprettResultat importStats(
      List<? extends Sykefraværsstatistikk> stats,
      ÅrstallOgKvartal årstallOgKvartal,
      SykefraværsstatistikkIntegrasjonUtils sykefraværsstatistikkIntegrasjonUtils) {

    int antallSletet = slett(årstallOgKvartal, sykefraværsstatistikkIntegrasjonUtils.getDeleteFunction());
    int antallOprettet = opprett(stats, sykefraværsstatistikkIntegrasjonUtils.getCreateFunction());

    return new SlettOgOpprettResultat(antallSletet, antallOprettet);
  }


  private int slett(ÅrstallOgKvartal årstallOgKvartal, DeleteSykefraværsstatistikkFunction deleteFunction) {
      int antallSlettet = deleteFunction.apply(årstallOgKvartal);
      return antallSlettet;
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
