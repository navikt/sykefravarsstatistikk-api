package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori.TOTAL
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhetMedGradering
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import java.math.BigDecimal
import java.math.BigDecimal.ZERO

object SykefraværsstatistikkImporteringUtils {
    fun genererSykefraværsstatistikkVirksomhet(
        gjeldendeKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkVirksomhet> {
        return HardkodetKildeTilVirksomhetsdata.hentVirksomheter(gjeldendeKvartal).flatMap {
            val muligeDagsverk = (0..100_00).random()
            val tapteDagsverkØvreGrense = muligeDagsverk / 6
            val renTilfeldighet = it.orgnr.verdi.last() == '9'

            Varighetskategori.entries.map { varighet ->
                val tapteDagsverk = (0..tapteDagsverkØvreGrense).random()

                SykefraværsstatistikkVirksomhet(
                    årstall = gjeldendeKvartal.årstall,
                    kvartal = gjeldendeKvartal.kvartal,
                    orgnr = it.orgnr.verdi,
                    varighet = varighet.kode,
                    rectype = it.rectype!!,
                    antallPersoner = if (renTilfeldighet) 0 else (0..500).random(),
                    tapteDagsverk = if (varighet != TOTAL) tapteDagsverk.toBigDecimal() else ZERO,
                    muligeDagsverk = if (varighet == TOTAL) muligeDagsverk.toBigDecimal() else ZERO,
                )
            }
        }
    }

    fun genererSykefraværsstatistikkVirksomhetMedGradering(sisteKvartal: ÅrstallOgKvartal): List<SykefraværsstatistikkVirksomhetMedGradering> {
        return listOf(
            SykefraværsstatistikkVirksomhetMedGradering(
                årstall = sisteKvartal.årstall,
                kvartal = sisteKvartal.kvartal,
                orgnr = "910562452",
                næring = "88",
                næringkode = "88911",
                rectype = "2",
                antallGraderteSykemeldinger = 6,
                tapteDagsverkGradertSykemelding = BigDecimal("87.353898"),
                antallSykemeldinger = 35,
                antallPersoner = 45,
                tapteDagsverk = BigDecimal("257.287398"),
                muligeDagsverk = BigDecimal("2149.569700")
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
