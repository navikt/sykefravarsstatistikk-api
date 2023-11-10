package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhetMedGradering
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import java.math.BigDecimal

object SykefraværsstatistikkImporteringUtils {
    fun genererSykefraværsstatistikkVirksomhet(
        sisteKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkVirksomhet> {
        return listOf(
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562452",
                'A',
                "2",
                0,
                BigDecimal.valueOf(23.333500),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562452",
                'B',
                "2",
                0,
                BigDecimal.valueOf(28.000000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562452",
                'C',
                "2",
                0,
                BigDecimal.valueOf(58.920000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562452",
                'D',
                "2",
                0,
                BigDecimal.valueOf(147.033898),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562452",
                'X',
                "2",
                45,
                BigDecimal.valueOf(0.000000),
                BigDecimal.valueOf(2149.569700)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311874411",
                'A',
                "2",
                0,
                BigDecimal.valueOf(1.440000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311874411",
                'B',
                "2",
                0,
                BigDecimal.valueOf(15.010000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311874411",
                'C',
                "2",
                0,
                BigDecimal.valueOf(59.625000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311874411",
                'D',
                "2",
                0,
                BigDecimal.valueOf(126.919576),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311874411",
                'E',
                "2",
                0,
                BigDecimal.valueOf(75.614300),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311874411",
                'F',
                "2",
                0,
                BigDecimal.valueOf(1.200000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311874411",
                'X',
                "2",
                59,
                BigDecimal.valueOf(0.000000),
                BigDecimal.valueOf(2049.160900)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "315829062",
                'A',
                "2",
                0,
                BigDecimal.valueOf(9.133400),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "315829062",
                'C',
                "2",
                0,
                BigDecimal.valueOf(20.969600),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "315829062",
                'D',
                "2",
                0,
                BigDecimal.valueOf(39.537600),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "315829062",
                'X',
                "2",
                49,
                BigDecimal.valueOf(0.000000),
                BigDecimal.valueOf(1333.733200)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311545795",
                'A',
                "2",
                0,
                BigDecimal.valueOf(12.600000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311545795",
                'B',
                "2",
                0,
                BigDecimal.valueOf(14.000000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311545795",
                'C',
                "2",
                0,
                BigDecimal.valueOf(4.600000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311545795",
                'D',
                "2",
                0,
                BigDecimal.valueOf(75.018182),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311545795",
                'F',
                "2",
                0,
                BigDecimal.valueOf(14.800000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311545795",
                'X',
                "2",
                43,
                BigDecimal.valueOf(0.000000),
                BigDecimal.valueOf(1831.816000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "312679671",
                'A',
                "2",
                0,
                BigDecimal.valueOf(5.800000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "312679671",
                'B',
                "2",
                0,
                BigDecimal.valueOf(22.500000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "312679671",
                'C',
                "2",
                0,
                BigDecimal.valueOf(17.400000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "312679671",
                'D',
                "2",
                0,
                BigDecimal.valueOf(1.000000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "312679671",
                'F',
                "2",
                0,
                BigDecimal.valueOf(112.100000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "312679671",
                'X',
                "2",
                59,
                BigDecimal.valueOf(0.000000),
                BigDecimal.valueOf(3208.506000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562436",
                'A',
                "2",
                0,
                BigDecimal.valueOf(16.000000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562436",
                'B',
                "2",
                0,
                BigDecimal.valueOf(61.000000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562436",
                'C',
                "2",
                0,
                BigDecimal.valueOf(67.700000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562436",
                'D',
                "2",
                0,
                BigDecimal.valueOf(355.151638),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562436",
                'E',
                "2",
                0,
                BigDecimal.valueOf(250.400000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562436",
                'F',
                "2",
                0,
                BigDecimal.valueOf(59.000000),
                BigDecimal.valueOf(0.000000)
            ),
            SykefraværsstatistikkVirksomhet(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562436",
                'X',
                "2",
                469,
                BigDecimal.valueOf(0.000000),
                BigDecimal.valueOf(26694.646600)
            )
        )
    }

    fun genererSykefraværsstatistikkVirksomhetMedGradering(sisteKvartal: ÅrstallOgKvartal): List<SykefraværsstatistikkVirksomhetMedGradering> {
        return listOf(
            SykefraværsstatistikkVirksomhetMedGradering(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562452",
                "88",
                "88911",
                "2",
                6,
                BigDecimal("87.353898"),
                35,
                45,
                BigDecimal("257.287398"),
                BigDecimal("2149.569700")
            ),
            SykefraværsstatistikkVirksomhetMedGradering(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311874411",
                "87",
                "87102",
                "2",
                7,
                BigDecimal("44.954576"),
                21,
                59,
                BigDecimal("279.808876"),
                BigDecimal("2049.160900")
            ),
            SykefraværsstatistikkVirksomhetMedGradering(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "315829062",
                "87",
                "87102",
                "2",
                0,
                BigDecimal("0.000000"),
                9,
                49,
                BigDecimal("69.640600"),
                BigDecimal("1333.733200")
            ),
            SykefraværsstatistikkVirksomhetMedGradering(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "311545795",
                "88",
                "88911",
                "2",
                5,
                BigDecimal("83.618182"),
                10,
                43,
                BigDecimal("121.018182"),
                BigDecimal("1831.816000")
            ),
            SykefraværsstatistikkVirksomhetMedGradering(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "312679671",
                "88",
                "88998",
                "2",
                2,
                BigDecimal("70.500000"),
                11,
                59,
                BigDecimal("158.800000"),
                BigDecimal("3208.506000")
            ),
            SykefraværsstatistikkVirksomhetMedGradering(
                sisteKvartal.årstall,
                sisteKvartal.kvartal,
                "910562436",
                "06",
                "06100",
                "2",
                13,
                BigDecimal("243.251638"),
                40,
                469,
                BigDecimal("809.251638"),
                BigDecimal("26694.646600")
            )
        )
    }
}
