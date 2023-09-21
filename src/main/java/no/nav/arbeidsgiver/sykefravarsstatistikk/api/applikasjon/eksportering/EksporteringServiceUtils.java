package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@Deprecated
public class EksporteringServiceUtils {

  public static final int OPPDATER_VIRKSOMHETER_SOM_ER_EKSPORTERT_BATCH_STØRRELSE = 1000;
  public static final int EKSPORT_BATCH_STØRRELSE = 10000;

  @NotNull
  public static List<SykefraværsstatistikkVirksomhetUtenVarighet> filterByKvartal(
      ÅrstallOgKvartal årstallOgKvartal,
      List<SykefraværsstatistikkVirksomhetUtenVarighet>
          sykefraværsstatistikkVirksomhetUtenVarighet) {
    return sykefraværsstatistikkVirksomhetUtenVarighet.stream()
        .filter(
            sfVirksomhet ->
                sfVirksomhet.getårstall() == årstallOgKvartal.getÅrstall()
                    && sfVirksomhet.getKvartal() == årstallOgKvartal.getKvartal())
        .collect(Collectors.toList());
  }

  public static UmaskertSykefraværForEttKvartal hentSisteKvartalIBeregningen(
      List<UmaskertSykefraværForEttKvartal> umaskertSykefraværsstatistikkSiste4Kvartaler,
      ÅrstallOgKvartal årstallOgKvartal) {
    return umaskertSykefraværsstatistikkSiste4Kvartaler.stream()
        .filter(u -> u.getårstallOgKvartal().equals(årstallOgKvartal))
        .findFirst()
        .orElse(null);
  }

  public static SykefraværsstatistikkLand mapToSykefraværsstatistikkLand(
      UmaskertSykefraværForEttKvartal umaskertSykefraværForEttKvartal) {
    return new SykefraværsstatistikkLand(
        umaskertSykefraværForEttKvartal.getÅrstall(),
        umaskertSykefraværForEttKvartal.getKvartal(),
        umaskertSykefraværForEttKvartal.getAntallPersoner(),
        umaskertSykefraværForEttKvartal.getDagsverkTeller(),
        umaskertSykefraværForEttKvartal.getDagsverkNevner());
  }

  public static Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> toMap(
      List<SykefraværsstatistikkVirksomhetUtenVarighet>
          sykefraværsstatistikkVirksomhetUtenVarighet) {
    Map<String, SykefraværsstatistikkVirksomhetUtenVarighet> map = new HashMap<>();
    sykefraværsstatistikkVirksomhetUtenVarighet.forEach(sf -> map.put(sf.getOrgnr(), sf));

    return map;
  }

  @NotNull
  public static Map<String, VirksomhetMetadata> getVirksomhetMetadataHashMap(
      @NotNull List<VirksomhetMetadata> virksomhetMetadataListe) {
    HashMap<String, VirksomhetMetadata> virksomhetMetadataHashMap = new HashMap<>();
    virksomhetMetadataListe.forEach(v -> virksomhetMetadataHashMap.put(v.getOrgnr(), v));

    return virksomhetMetadataHashMap;
  }

  @NotNull
  public static List<VirksomhetMetadata> getVirksomheterMetadataFraSubset(
      Map<String, VirksomhetMetadata> virksomhetMetadataHashMap,
      List<VirksomhetEksportPerKvartal> subset) {
    List<VirksomhetMetadata> virksomheterMetadata = new ArrayList<>();
    subset.forEach(
        v -> {
          if (virksomhetMetadataHashMap.containsKey(v.getOrgnr())) {
            virksomheterMetadata.add(virksomhetMetadataHashMap.get(v.getOrgnr()));
          }
        });

    return virksomheterMetadata;
  }


  public static VirksomhetSykefravær getVirksomhetSykefravær(
      VirksomhetMetadata virksomhetMetadata,
      Map<String, SykefraværsstatistikkVirksomhetUtenVarighet>
          sykefraværsstatistikkVirksomhetUtenVarighet) {
    SykefraværsstatistikkVirksomhetUtenVarighet sfStatistikk =
        sykefraværsstatistikkVirksomhetUtenVarighet.get(virksomhetMetadata.getOrgnr());

    return new VirksomhetSykefravær(
        virksomhetMetadata.getOrgnr(),
            virksomhetMetadata.getNavn(),
        new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
        sfStatistikk != null ? sfStatistikk.getTapteDagsverk() : null,
        sfStatistikk != null ? sfStatistikk.getMuligeDagsverk() : null,
        sfStatistikk != null ? sfStatistikk.getAntallPersoner() : 0);
  }

  public static SykefraværMedKategori getSykefraværMedKategoriForLand(
      ÅrstallOgKvartal årstallOgKvartal, SykefraværsstatistikkLand sykefraværsstatistikkLand) {
    return new SykefraværMedKategori(
        Statistikkategori.LAND,
        "NO",
        årstallOgKvartal,
            sykefraværsstatistikkLand.getTapteDagsverk(),
            sykefraværsstatistikkLand.getMuligeDagsverk(),
            sykefraværsstatistikkLand.getAntallPersoner());
  }

  public static SykefraværMedKategori getSykefraværMedKategoriForSektor(
      VirksomhetMetadata virksomhetMetadata,
      List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor) {
    SykefraværsstatistikkSektor sfSektor =
        sykefraværsstatistikkSektor.stream()
            .filter(
                v ->
                    v.getSektorkode().equals(virksomhetMetadata.getSektor().getSektorkode())
                        && v.getårstall() == virksomhetMetadata.getÅrstall()
                        && v.getKvartal() == virksomhetMetadata.getKvartal())
            .collect(
                toSingleton(
                    new SykefraværsstatistikkSektor(
                        virksomhetMetadata.getÅrstall(),
                        virksomhetMetadata.getKvartal(),
                            virksomhetMetadata.getSektor().getSektorkode(),
                        0,
                        null,
                        null)));

    return new SykefraværMedKategori(
        Statistikkategori.SEKTOR,
            sfSektor.getSektorkode(),
        new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
            sfSektor.getTapteDagsverk(),
            sfSektor.getMuligeDagsverk(),
            sfSektor.getAntallPersoner());
  }

  public static SykefraværMedKategori getSykefraværMedKategoriNæringForVirksomhet(
      VirksomhetMetadata virksomhetMetadata,
      List<SykefraværsstatistikkForNæring> sykefraværsstatistikkForNæring) {
    SykefraværsstatistikkForNæring sfNæring =
        sykefraværsstatistikkForNæring.stream()
            .filter(
                v ->
                    v.getNæringkode().equals(virksomhetMetadata.getPrimærnæring())
                        && v.getårstall() == virksomhetMetadata.getÅrstall()
                        && v.getKvartal() == virksomhetMetadata.getKvartal())
            .collect(
                toSingleton(
                    new SykefraværsstatistikkForNæring(
                        virksomhetMetadata.getÅrstall(),
                        virksomhetMetadata.getKvartal(),
                            virksomhetMetadata.getPrimærnæring(),
                        0,
                        null,
                        null)));

    return new SykefraværMedKategori(
        Statistikkategori.NÆRING,
        sfNæring.getNæringkode(),
        new ÅrstallOgKvartal(virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
        sfNæring.getTapteDagsverk(),
        sfNæring.getMuligeDagsverk(),
        sfNæring.getAntallPersoner());
  }

  public static List<SykefraværMedKategori> getSykefraværMedKategoriForNæring5Siffer(
      VirksomhetMetadata virksomhetMetadata,
      List<SykefraværsstatistikkForNæringskode> sykefraværsstatistikkForNæringskodeList) {

    List<SykefraværsstatistikkForNæringskode> filteredList =
        getSykefraværsstatistikkNæring5Siffers(
            virksomhetMetadata, sykefraværsstatistikkForNæringskodeList);

    List<SykefraværMedKategori> resultatList = new ArrayList<>();

    filteredList.forEach(
        sfNæring5Siffer ->
            resultatList.add(
                new SykefraværMedKategori(
                    Statistikkategori.NÆRINGSKODE,
                        sfNæring5Siffer.getNæringkode5siffer(),
                    new ÅrstallOgKvartal(
                        virksomhetMetadata.getÅrstall(), virksomhetMetadata.getKvartal()),
                        sfNæring5Siffer.getTapteDagsverk(),
                        sfNæring5Siffer.getMuligeDagsverk(),
                        sfNæring5Siffer.getAntallPersoner())));

    return resultatList;
  }

  @NotNull
  public static List<SykefraværsstatistikkForNæringskode> getSykefraværsstatistikkNæring5Siffers(
      VirksomhetMetadata virksomhetMetadata,
      List<SykefraværsstatistikkForNæringskode> sykefraværsstatistikkForNæringskodeList) {
    return sykefraværsstatistikkForNæringskodeList.stream()
        .filter(
            næring5Siffer ->
                virksomhetMetadata.getNæringOgNæringskode5siffer().stream()
                        .anyMatch(
                            næringskode ->
                                næring5Siffer.getNæringkode5siffer()
                                    .equals(næringskode.getFemsifferIdentifikator()))
                    && næring5Siffer.getårstall() == virksomhetMetadata.getÅrstall()
                    && næring5Siffer.getKvartal() == virksomhetMetadata.getKvartal())
        .collect(Collectors.toList());
  }

  public static void cleanUpEtterBatch(EksporteringRepository eksporteringRepository) {
    eksporteringRepository.oppdaterAlleVirksomheterIEksportTabellSomErBekrreftetEksportert();
    eksporteringRepository.slettVirksomheterBekreftetEksportert();
  }

  public static int leggTilOrgnrIEksporterteVirksomheterListaOglagreIDbNårListaErFull(
          String orgnr,
          ÅrstallOgKvartal årstallOgKvartal,
          @NotNull List<String> virksomheterSomSkalFlaggesSomEksportert,
          EksporteringRepository eksporteringRepository,
          KafkaClient kafkaClient) {
    virksomheterSomSkalFlaggesSomEksportert.add(orgnr);

    if (virksomheterSomSkalFlaggesSomEksportert.size()
        == OPPDATER_VIRKSOMHETER_SOM_ER_EKSPORTERT_BATCH_STØRRELSE) {
      return lagreEksporterteVirksomheterOgNullstillLista(
          årstallOgKvartal,
          virksomheterSomSkalFlaggesSomEksportert,
          eksporteringRepository,
              kafkaClient);
    } else {
      return 0;
    }
  }

  public static int lagreEksporterteVirksomheterOgNullstillLista(
          ÅrstallOgKvartal årstallOgKvartal,
          List<String> virksomheterSomSkalFlaggesSomEksportert,
          EksporteringRepository eksporteringRepository,
          KafkaClient kafkaClient) {
    int antallSomSkalOppdateres = virksomheterSomSkalFlaggesSomEksportert.size();
    long startWriteToDB = System.nanoTime();
    eksporteringRepository.batchOpprettVirksomheterBekreftetEksportert(
        virksomheterSomSkalFlaggesSomEksportert, årstallOgKvartal);
    virksomheterSomSkalFlaggesSomEksportert.clear();
    long stopWriteToDB = System.nanoTime();

    kafkaClient.addDBOppdateringProcessingTime(startWriteToDB, stopWriteToDB);

    return antallSomSkalOppdateres;
  }

  @NotNull
  public static List<VirksomhetEksportPerKvartal> getListeAvVirksomhetEksportPerKvartal(
      ÅrstallOgKvartal årstallOgKvartal,
      EksporteringRepository eksporteringRepository) {
    List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartal =
        eksporteringRepository.hentVirksomhetEksportPerKvartal(årstallOgKvartal);

    Stream<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartalStream =
        virksomhetEksportPerKvartal.stream().filter(v -> !v.eksportert());

    return virksomhetEksportPerKvartalStream.collect(Collectors.toList());
  }

  private static <T> Collector<T, ?, T> toSingleton(T emptySykefraværsstatistikk) {
    return Collectors.collectingAndThen(
        Collectors.toList(),
        list -> {
          if (list.size() != 1) {
            return emptySykefraværsstatistikk;
          }
          return list.get(0);
        });
  }
}
