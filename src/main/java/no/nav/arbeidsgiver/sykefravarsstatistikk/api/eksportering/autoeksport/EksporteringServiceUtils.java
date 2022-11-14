package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.EitherUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Sykefraværsstatistikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkLand;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetUtenVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.Aggregeringskalkulator;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.StatistikkDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.Sykefraværsdata;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EksporteringServiceUtils {


    public static final int OPPDATER_VIRKSOMHETER_SOM_ER_EKSPORTERT_BATCH_STØRRELSE = 1000;
    public static final int EKSPORT_BATCH_STØRRELSE = 10000;

    @NotNull
    protected static List<SykefraværsstatistikkVirksomhetUtenVarighet> filterByKvartal(
            ÅrstallOgKvartal årstallOgKvartal,
            List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet
    ) {
        return sykefraværsstatistikkVirksomhetUtenVarighet.stream().filter(
              sfVirksomhet ->
                    sfVirksomhet.getÅrstall() == årstallOgKvartal.getÅrstall() &&
                          sfVirksomhet.getKvartal() == årstallOgKvartal.getKvartal()
        ).collect(Collectors.toList());
    }

    protected static UmaskertSykefraværForEttKvartal hentSisteKvartalIBeregningen(
          List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4KvartalerLand, ÅrstallOgKvartal årstallOgKvartal) {
        return umaskertSykefraværsstatistikkSiste4KvartalerLand.
              stream().
              filter(u ->
                    u.getÅrstallOgKvartal().equals(årstallOgKvartal)
              ).findFirst().orElse(null);
    }

    protected static SykefraværsstatistikkLand mapToSykefraværsstatistikkLand(UmaskertSykefraværForEttKvartal umaskertSykefraværForEttKvartal) {
        return new SykefraværsstatistikkLand(
              umaskertSykefraværForEttKvartal.getÅrstall(),
              umaskertSykefraværForEttKvartal.getKvartal(),
              umaskertSykefraværForEttKvartal.getAntallPersoner(),
              umaskertSykefraværForEttKvartal.getDagsverkTeller(),
              umaskertSykefraværForEttKvartal.getDagsverkNevner()
        );
    }

    protected static Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> toMap(
            List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet
    ) {
        Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> map = new HashMap<>();
        sykefraværsstatistikkVirksomhetUtenVarighet.forEach(sf -> {
            map.put(sf.getOrgnr(), sf);
        });

        return map;
    }

    @NotNull
    protected static Map<String, VirksomhetMetadata> getVirksomhetMetadataHashMap(
          @NotNull List<VirksomhetMetadata> virksomhetMetadataListe
    ) {
        HashMap<String, VirksomhetMetadata> virksomhetMetadataHashMap = new HashMap<>();
        virksomhetMetadataListe.stream().forEach(
              v -> virksomhetMetadataHashMap.put(v.getOrgnr(), v)
        );

        return virksomhetMetadataHashMap;
    }

    @NotNull
    protected static List<VirksomhetMetadata> getVirksomheterMetadataFraSubset(
          Map<String, VirksomhetMetadata> virksomhetMetadataHashMap,
          List<VirksomhetEksportPerKvartal> subset
    ) {
        List<VirksomhetMetadata> virksomheterMetadata = new ArrayList<>();
        subset.stream().forEach(v -> {
            if (virksomhetMetadataHashMap.containsKey(v.getOrgnr())) {
                virksomheterMetadata.add(virksomhetMetadataHashMap.get(v.getOrgnr()));
            }
        });

        return virksomheterMetadata;
    }

    protected static long getAntallSomKanEksporteres(List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartal) {
        return virksomhetEksportPerKvartal.stream().filter(v -> !v.eksportert()).count();
    }

    // TODO: er denne bare til test?
    protected static VirksomhetMetadata getVirksomhetMetada(
          Orgnr orgnr,
          List<VirksomhetMetadata> virksomhetMetadataSet
    ) {
        List<VirksomhetMetadata> virksomhetMetadataFunnet =
              virksomhetMetadataSet.stream().filter(
                    v -> v.getOrgnr().equals(orgnr.getVerdi())
              ).collect(Collectors.toList());

        if (virksomhetMetadataFunnet.size() != 1) {
            return null;
        } else {
            return virksomhetMetadataFunnet.get(0);
        }
    }

    // TODO: bare brukt til tests?
    protected static VirksomhetSykefravær getVirksomhetSykefravær(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet
    ) {
        SykefraværsstatistikkVirksomhetUtenVarighet sfStatistikk =
              sykefraværsstatistikkVirksomhetUtenVarighet.stream().filter(
                    v -> v.getOrgnr().equals(virksomhetMetadata.getOrgnr())
                          && v.getÅrstall() == virksomhetMetadata.getÅrstall()
                          && v.getKvartal() == virksomhetMetadata.getKvartal()
              ).collect(toSingleton(
                    new SykefraværsstatistikkVirksomhetUtenVarighet(
                          virksomhetMetadata.getÅrstall(),
                          virksomhetMetadata.getKvartal(),
                          virksomhetMetadata.getOrgnr(),
                          0,
                          null,
                          null
                    )
              ));

        return new VirksomhetSykefravær(
              virksomhetMetadata.getOrgnr(),
              virksomhetMetadata.getNavn(),
              new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
              sfStatistikk.getTapteDagsverk(),
              sfStatistikk.getMuligeDagsverk(),
              sfStatistikk.getAntallPersoner()
        );
    }

    protected static VirksomhetSykefravær getVirksomhetSykefravær(
          VirksomhetMetadata virksomhetMetadata,
          Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet
    ) {
        SykefraværsstatistikkVirksomhetUtenVarighet sfStatistikk =
              sykefraværsstatistikkVirksomhetUtenVarighet.get(virksomhetMetadata.getOrgnr());

        return new VirksomhetSykefravær(
              virksomhetMetadata.getOrgnr(),
              virksomhetMetadata.getNavn(),
              new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
              sfStatistikk != null ? sfStatistikk.getTapteDagsverk() : null,
              sfStatistikk != null ? sfStatistikk.getMuligeDagsverk() : null,
              sfStatistikk != null ? sfStatistikk.getAntallPersoner() : 0
        );
    }

    protected static SykefraværMedKategori getSykefraværMedKategoriForLand(
          ÅrstallOgKvartal årstallOgKvartal,
          SykefraværsstatistikkLand sykefraværsstatistikkLand
    ) {
        return new SykefraværMedKategori(
              Statistikkategori.LAND,
              "NO",
              årstallOgKvartal,
              sykefraværsstatistikkLand.getTapteDagsverk(),
              sykefraværsstatistikkLand.getMuligeDagsverk(),
              sykefraværsstatistikkLand.getAntallPersoner()
        );
    }

    protected static SykefraværMedKategori getSykefraværMedKategoriForSektor(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor
    ) {
        SykefraværsstatistikkSektor sfSektor =
              sykefraværsstatistikkSektor.stream().filter(
                    v -> v.getSektorkode().equals(virksomhetMetadata.getSektor())
                          && v.getÅrstall() == virksomhetMetadata.getÅrstall()
                          && v.getKvartal() == virksomhetMetadata.getKvartal()
              ).collect(toSingleton(
                    new SykefraværsstatistikkSektor(
                          virksomhetMetadata.getÅrstall(),
                          virksomhetMetadata.getKvartal(),
                          virksomhetMetadata.getSektor(),
                          0,
                          null,
                          null
                    )
              ));

        return new SykefraværMedKategori(
              Statistikkategori.SEKTOR,
              sfSektor.getSektorkode(),
              new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
              sfSektor.getTapteDagsverk(),
              sfSektor.getMuligeDagsverk(),
              sfSektor.getAntallPersoner()
        );
    }

    protected static SykefraværMedKategori getSykefraværMedKategoriForNæring(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring
    ) {
        SykefraværsstatistikkNæring sfNæring =
              sykefraværsstatistikkNæring.stream().filter(
                    v -> v.getNæringkode().equals(virksomhetMetadata.getNæring())
                          && v.getÅrstall() == virksomhetMetadata.getÅrstall()
                          && v.getKvartal() == virksomhetMetadata.getKvartal()
              ).collect(toSingleton(
                    new SykefraværsstatistikkNæring(
                          virksomhetMetadata.getÅrstall(),
                          virksomhetMetadata.getKvartal(),
                          virksomhetMetadata.getNæring(),
                          0,
                          null,
                          null
                    )
              ));

        return new SykefraværMedKategori(
              Statistikkategori.NÆRING2SIFFER,
              sfNæring.getNæringkode(),
              new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
              sfNæring.getTapteDagsverk(),
              sfNæring.getMuligeDagsverk(),
              sfNæring.getAntallPersoner()
        );
    }

    protected static List<StatistikkDto> getSykefraværMedKategoriForBransjeEllerNæringSiste4Kvartaler(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring,
          List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5Siffer,
          ÅrstallOgKvartal fraÅrstallOgKvartal,
          BransjeEllerNæring bransjeEllerNæring
    ) {
        Aggregeringskalkulator aggregeringskalkulator;
        if (bransjeEllerNæring.isBransje()) {
            List<UmaskertSykefraværForEttKvartal> sykefraværsstatistikkBransje
                  = sykefraværsstatistikkNæring5Siffer.stream().filter(
                        br -> bransjeEllerNæring.getBransje().getKoderSomSpesifisererNæringer()
                              .contains(br.getNæringkode5siffer())
                              && erIKvartalerIberegningen(fraÅrstallOgKvartal, br)
                  ).map(EksporteringServiceUtils::mapTilUmaskertSykefraværForEttKvartal)
                  .collect(Collectors.toList());
            aggregeringskalkulator = new Aggregeringskalkulator(
                  new Sykefraværsdata(Map.of(Statistikkategori.BRANSJE, sykefraværsstatistikkBransje)),fraÅrstallOgKvartal.plussKvartaler(3)
            );

        } else {
            List<UmaskertSykefraværForEttKvartal> umaskertSykefraværForEttKvartaler =
                  sykefraværsstatistikkNæring.stream().filter(
                        v -> v.getNæringkode().equals(virksomhetMetadata.getNæring())
                              && erIKvartalerIberegningen(fraÅrstallOgKvartal, v)
                  ).map(
                        EksporteringServiceUtils::mapTilUmaskertSykefraværForEttKvartal
                  ).collect(Collectors.toList());
            aggregeringskalkulator = new Aggregeringskalkulator(
                  new Sykefraværsdata(
                        Map.of(Statistikkategori.NÆRING, umaskertSykefraværForEttKvartaler)
                  ),fraÅrstallOgKvartal.plussKvartaler(3)
            );
        }
        return EitherUtils.filterRights(aggregeringskalkulator.fraværsprosentBransjeEllerNæring(bransjeEllerNæring));
    }

    protected static List<SykefraværMedKategori> getSykefraværMedKategoriForNæring5Siffer(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5SifferList
    ) {

        List<SykefraværsstatistikkNæring5Siffer> filteredList =
              getSykefraværsstatistikkNæring5Siffers(virksomhetMetadata, sykefraværsstatistikkNæring5SifferList);

        List<SykefraværMedKategori> resultatList = new ArrayList();

        filteredList.stream().forEach(
              sfNæring5Siffer -> {
                  resultatList.add(
                        new SykefraværMedKategori(
                              Statistikkategori.NÆRING5SIFFER,
                              sfNæring5Siffer.getNæringkode5siffer(),
                              new ÅrstallOgKvartal(
                                    virksomhetMetadata.getÅrstall(),
                                    virksomhetMetadata.getKvartal()
                              ),
                              sfNæring5Siffer.getTapteDagsverk(),
                              sfNæring5Siffer.getMuligeDagsverk(),
                              sfNæring5Siffer.getAntallPersoner()
                        )
                  );
              }
        );

        return resultatList;
    }

    protected static List<StatistikkDto> getSykefraværMedKategoriVirksomhetSiste4Kvartaler(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkVirksomhetUtenVarighet> sykefraværsstatistikkVirksomhetUtenVarighet,
          ÅrstallOgKvartal fraÅrstallOgKvartal
    ) {
        List<UmaskertSykefraværForEttKvartal> umaskertSykefraværForEttKvartal
              = sykefraværsstatistikkVirksomhetUtenVarighet.stream().filter(
                    u -> Objects.equals(u.getOrgnr(), virksomhetMetadata.getOrgnr()) &&
                          erIKvartalerIberegningen(fraÅrstallOgKvartal, u)
              ).map(EksporteringServiceUtils::mapTilUmaskertSykefraværForEttKvartal)
              .collect(Collectors.toList());
        Aggregeringskalkulator aggregeringskalkulatorVirksomhet
              = new Aggregeringskalkulator(
              new Sykefraværsdata(Map.of(Statistikkategori.VIRKSOMHET, umaskertSykefraværForEttKvartal)),fraÅrstallOgKvartal.plussKvartaler(3));
        return EitherUtils.filterRights(
              aggregeringskalkulatorVirksomhet.fraværsprosentVirksomhet(virksomhetMetadata.getNavn())
        );
    }

    @NotNull
    protected static List<SykefraværsstatistikkNæring5Siffer> getSykefraværsstatistikkNæring5Siffers(
          VirksomhetMetadata virksomhetMetadata,
          List<SykefraværsstatistikkNæring5Siffer> sykefraværsstatistikkNæring5SifferList
    ) {
        List<SykefraværsstatistikkNæring5Siffer> filteredList = sykefraværsstatistikkNæring5SifferList.stream()
              .filter(næring5Siffer -> virksomhetMetadata.getNæringOgNæringskode5siffer().stream()
                    .anyMatch(virksomhetNæring5Siffer ->
                          næring5Siffer.getNæringkode5siffer().equals(
                                virksomhetNæring5Siffer.getNæringskode5Siffer()
                          )
                    )
                    && næring5Siffer.getÅrstall() == virksomhetMetadata.getÅrstall()
                    && næring5Siffer.getKvartal() == virksomhetMetadata.getKvartal()
              )
              .collect(Collectors.toList());
        return filteredList;
    }

    @NotNull
    protected static void cleanUpEtterBatch(EksporteringRepository eksporteringRepository) {
        eksporteringRepository.oppdaterAlleVirksomheterIEksportTabellSomErBekrreftetEksportert();
        eksporteringRepository.slettVirksomheterBekreftetEksportert();
    }

    @NotNull
    protected static int leggTilOrgnrIEksporterteVirksomheterListaOglagreIDbNårListaErFull(
        String orgnr,
        ÅrstallOgKvartal årstallOgKvartal,
        @NotNull List<String> virksomheterSomSkalFlaggesSomEksportert,
        EksporteringRepository eksporteringRepository,
        KafkaService kafkaService
    ) {
        virksomheterSomSkalFlaggesSomEksportert.add(orgnr);

        if (virksomheterSomSkalFlaggesSomEksportert.size()
            == OPPDATER_VIRKSOMHETER_SOM_ER_EKSPORTERT_BATCH_STØRRELSE
        ) {
            return lagreEksporterteVirksomheterOgNullstillLista(
                årstallOgKvartal,
                virksomheterSomSkalFlaggesSomEksportert,
                eksporteringRepository,
                kafkaService
            );
        } else {
            return 0;
        }
    }

    @NotNull
    protected static int lagreEksporterteVirksomheterOgNullstillLista(
        ÅrstallOgKvartal årstallOgKvartal,
        List<String> virksomheterSomSkalFlaggesSomEksportert,
        EksporteringRepository eksporteringRepository,
        KafkaService kafkaService
    ) {
        int antallSomSkalOppdateres = virksomheterSomSkalFlaggesSomEksportert.size();
        long startWriteToDB = System.nanoTime();
        eksporteringRepository.batchOpprettVirksomheterBekreftetEksportert(
            virksomheterSomSkalFlaggesSomEksportert,
            årstallOgKvartal
        );
        virksomheterSomSkalFlaggesSomEksportert.clear();
        long stopWriteToDB = System.nanoTime();

        kafkaService.addDBOppdateringProcessingTime(startWriteToDB, stopWriteToDB);

        return antallSomSkalOppdateres;
    }


    @NotNull
    protected static List<VirksomhetEksportPerKvartal> getListeAvVirksomhetEksportPerKvartal(
        ÅrstallOgKvartal årstallOgKvartal,
        EksporteringBegrensning eksporteringBegrensning,
        EksporteringRepository eksporteringRepository
    ) {
        List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartal =
            eksporteringRepository.hentVirksomhetEksportPerKvartal(årstallOgKvartal);

        Stream<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartalStream = virksomhetEksportPerKvartal
            .stream()
            .filter(v -> !v.eksportert());

        return eksporteringBegrensning.erBegrenset() ?
            virksomhetEksportPerKvartalStream.
                limit(eksporteringBegrensning.getAntallSomSkalEksporteres())
                .collect(Collectors.toList()) :
            virksomhetEksportPerKvartalStream.collect(Collectors.toList());
    }

    private static boolean erIKvartalerIberegningen(ÅrstallOgKvartal fraÅrstallOgKvartal, Sykefraværsstatistikk v) {
        return (v.getÅrstall() > fraÅrstallOgKvartal.getÅrstall())
              ||
              (
                    v.getÅrstall() == fraÅrstallOgKvartal.getÅrstall()
                          && v.getKvartal() >= fraÅrstallOgKvartal.getKvartal()
              );
    }

    private static <T> Collector<T, ?, T> toSingleton(T emptySykefraværsstatistikk) {
        return Collectors.collectingAndThen(
              Collectors.toList(),
              list -> {
                  if (list.size() != 1) {
                      return emptySykefraværsstatistikk;
                  }
                  return list.get(0);
              }
        );
    }

    private static UmaskertSykefraværForEttKvartal mapTilUmaskertSykefraværForEttKvartal(SykefraværsstatistikkNæring sfsNæring) {
        return new UmaskertSykefraværForEttKvartal(
              new ÅrstallOgKvartal(sfsNæring.getÅrstall(), sfsNæring.getKvartal()),
              sfsNæring.getTapteDagsverk(),
              sfsNæring.getMuligeDagsverk(),
              sfsNæring.getAntallPersoner()
        );
    }

    private static UmaskertSykefraværForEttKvartal mapTilUmaskertSykefraværForEttKvartal(SykefraværsstatistikkNæring5Siffer sfsNæring) {
        return new UmaskertSykefraværForEttKvartal(
              new ÅrstallOgKvartal(sfsNæring.getÅrstall(), sfsNæring.getKvartal()),
              sfsNæring.getTapteDagsverk(),
              sfsNæring.getMuligeDagsverk(),
              sfsNæring.getAntallPersoner()
        );
    }

    protected static UmaskertSykefraværForEttKvartal mapTilUmaskertSykefraværForEttKvartal(
            SykefraværsstatistikkVirksomhetUtenVarighet sfsNæring
    ) {
        return new UmaskertSykefraværForEttKvartal(
              new ÅrstallOgKvartal(sfsNæring.getÅrstall(), sfsNæring.getKvartal()),
              sfsNæring.getTapteDagsverk(),
              sfsNæring.getMuligeDagsverk(),
              sfsNæring.getAntallPersoner()
        );
    }
}
