package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetMedGradering;

public class SykefraværsstatistikkImporteringUtils {

  public static List<SykefraværsstatistikkVirksomhet> genererSykefraværsstatistikkVirksomhet(
      ÅrstallOgKvartal sisteKvartal) {
    return Arrays.asList(
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "910562452", "A", "2", 0,
            BigDecimal.valueOf(23.333500), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "910562452", "B", "2", 0,
            BigDecimal.valueOf(28.000000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "910562452", "C", "2", 0,
            BigDecimal.valueOf(58.920000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "910562452", "D", "2", 0,
            BigDecimal.valueOf(147.033898), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "910562452", "X", "2", 45,
            BigDecimal.valueOf(0.000000), BigDecimal.valueOf(2149.569700)),

        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311874411", "A", "2", 0,
            BigDecimal.valueOf(1.440000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311874411", "B", "2", 0,
            BigDecimal.valueOf(15.010000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311874411", "C", "2", 0,
            BigDecimal.valueOf(59.625000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311874411", "D", "2", 0,
            BigDecimal.valueOf(126.919576), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311874411", "E", "2", 0,
            BigDecimal.valueOf(75.614300), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311874411", "F", "2", 0,
            BigDecimal.valueOf(1.200000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311874411", "X", "2", 59,
            BigDecimal.valueOf(0.000000), BigDecimal.valueOf(2049.160900)),

        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "315829062", "A", "2", 0,
            BigDecimal.valueOf(9.133400), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "315829062", "C", "2", 0,
            BigDecimal.valueOf(20.969600), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "315829062", "D", "2", 0,
            BigDecimal.valueOf(39.537600), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "315829062", "X", "2", 49,
            BigDecimal.valueOf(0.000000), BigDecimal.valueOf(1333.733200)),

        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311545795", "A", "2", 0,
            BigDecimal.valueOf(12.600000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311545795", "B", "2", 0,
            BigDecimal.valueOf(14.000000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311545795", "C", "2", 0,
            BigDecimal.valueOf(4.600000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311545795", "D", "2", 0,
            BigDecimal.valueOf(75.018182), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311545795", "F", "2", 0,
            BigDecimal.valueOf(14.800000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "311545795", "X", "2", 43,
            BigDecimal.valueOf(0.000000), BigDecimal.valueOf(1831.816000)),

        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "312679671", "A", "2", 0,
            BigDecimal.valueOf(5.800000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "312679671", "B", "2", 0,
            BigDecimal.valueOf(22.500000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "312679671", "C", "2", 0,
            BigDecimal.valueOf(17.400000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "312679671", "D", "2", 0,
            BigDecimal.valueOf(1.000000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "312679671", "F", "2", 0,
            BigDecimal.valueOf(112.100000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "312679671", "X", "2", 59,
            BigDecimal.valueOf(0.000000), BigDecimal.valueOf(3208.506000)),

        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "910562436", "A", "2", 0,
            BigDecimal.valueOf(16.000000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "910562436", "B", "2", 0,
            BigDecimal.valueOf(61.000000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "910562436", "C", "2", 0,
            BigDecimal.valueOf(67.700000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "910562436", "D", "2", 0,
            BigDecimal.valueOf(355.151638), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "910562436", "E", "2", 0,
            BigDecimal.valueOf(250.400000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "910562436", "F", "2", 0,
            BigDecimal.valueOf(59.000000), BigDecimal.valueOf(0.000000)),
        new SykefraværsstatistikkVirksomhet(sisteKvartal.getÅrstall(), sisteKvartal.getKvartal(),
            "910562436", "X", "2", 469,
            BigDecimal.valueOf(0.000000), BigDecimal.valueOf(26694.646600))
        );
  }

  public static List<SykefraværsstatistikkVirksomhetMedGradering> genererSykefraværsstatistikkVirksomhetMedGradering(
      ÅrstallOgKvartal sisteKvartal) {
    return Arrays.asList(
        new SykefraværsstatistikkVirksomhetMedGradering(sisteKvartal.getÅrstall(),
            sisteKvartal.getKvartal(), "910562452", "88", "88911", "2", 6,
            new BigDecimal("87.353898"), 35, 45, new BigDecimal("257.287398"),
            new BigDecimal("2149.569700")),

        new SykefraværsstatistikkVirksomhetMedGradering(sisteKvartal.getÅrstall(),
            sisteKvartal.getKvartal(), "311874411", "87", "87102", "2", 7,
            new BigDecimal("44.954576"), 21, 59, new BigDecimal("279.808876"),
            new BigDecimal("2049.160900")),

        new SykefraværsstatistikkVirksomhetMedGradering(sisteKvartal.getÅrstall(),
            sisteKvartal.getKvartal(), "315829062", "87", "87102", "2", 0,
            new BigDecimal("0.000000"), 9, 49, new BigDecimal("69.640600"),
            new BigDecimal("1333.733200")),

        new SykefraværsstatistikkVirksomhetMedGradering(sisteKvartal.getÅrstall(),
            sisteKvartal.getKvartal(), "311545795", "88", "88911", "2", 5,
            new BigDecimal("83.618182"), 10, 43, new BigDecimal("121.018182"),
            new BigDecimal("1831.816000")),

        new SykefraværsstatistikkVirksomhetMedGradering(sisteKvartal.getÅrstall(),
            sisteKvartal.getKvartal(), "312679671", "88", "88998", "2", 2,
            new BigDecimal("70.500000"), 11, 59, new BigDecimal("158.800000"),
            new BigDecimal("3208.506000")),

        new SykefraværsstatistikkVirksomhetMedGradering(sisteKvartal.getÅrstall(),
            sisteKvartal.getKvartal(), "910562436", "06", "06100", "2",
            13,
            new BigDecimal("243.251638"), 40, 469, new BigDecimal("809.251638"),
            new BigDecimal("26694.646600"))
    );
  }
}
