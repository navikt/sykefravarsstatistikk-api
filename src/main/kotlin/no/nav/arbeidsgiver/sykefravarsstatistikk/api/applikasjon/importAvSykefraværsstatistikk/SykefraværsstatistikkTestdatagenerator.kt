package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori.TOTAL
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhetMedGradering
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import java.math.BigDecimal.ZERO

object SykefraværsstatistikkTestdatagenerator {
    private val generertStatistikk = mutableMapOf<ÅrstallOgKvartal, List<SykefraværsstatistikkVirksomhet>>()
    private val generertStatistikkMedGradering =
        mutableMapOf<ÅrstallOgKvartal, List<SykefraværsstatistikkVirksomhetMedGradering>>()

    fun genererSykefraværsstatistikkVirksomhet(
        gjeldendeKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkVirksomhet> {

        generertStatistikk[gjeldendeKvartal]?.let {
            return it
        }

        return genererSykefraværsstatistikkVirksomhetMedGradering(gjeldendeKvartal).flatMap {
            Varighetskategori.entries.map { varighet ->

                val tapteDagsverkØvreGrense = it.tapteDagsverk.toInt() / 6
                val tapteDagsverk = (0..tapteDagsverkØvreGrense).random().toBigDecimal()

                SykefraværsstatistikkVirksomhet(
                    årstall = gjeldendeKvartal.årstall,
                    kvartal = gjeldendeKvartal.kvartal,
                    orgnr = it.orgnr,
                    varighet = varighet.kode,
                    rectype = it.rectype,
                    antallPersoner = if (varighet == TOTAL) it.antallPersoner else 0,
                    tapteDagsverk = if (varighet == TOTAL) ZERO else tapteDagsverk,
                    muligeDagsverk = if (varighet == TOTAL) it.muligeDagsverk else ZERO,
                )
            }
        }.also { generertStatistikk[gjeldendeKvartal] = it }
    }

    fun genererSykefraværsstatistikkVirksomhetMedGradering(gjeldendeKvartal: ÅrstallOgKvartal): List<SykefraværsstatistikkVirksomhetMedGradering> {
        generertStatistikkMedGradering[gjeldendeKvartal]?.let {
            return it
        }

        return HardkodetKildeTilVirksomhetsdata.hentTestvirksomheter(gjeldendeKvartal)
            .map {
                val tiProsentAvBedriftene = it.orgnr.verdi.last() == '9'
                val antallPersoner = if (tiProsentAvBedriftene) (40..10_000).random() else (0..40).random()

                val antallDagsverkIEttKvartal = 230 / 4
                val muligeDagsverk = antallPersoner * antallDagsverkIEttKvartal

                val tapteDagsverk = (0..muligeDagsverk).random().toBigDecimal()

                val antallSykemeldinger = (0..(antallPersoner * 1.25).toInt()).random()

                val graderingsfaktor = Math.random()

                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = gjeldendeKvartal.årstall,
                    kvartal = gjeldendeKvartal.kvartal,
                    orgnr = it.orgnr.verdi,
                    næring = it.næring!!,
                    næringkode = it.næringskode!!,
                    rectype = it.rectype!!,
                    antallGraderteSykemeldinger = (graderingsfaktor * antallSykemeldinger).toInt(),
                    tapteDagsverkGradertSykemelding = (graderingsfaktor.toBigDecimal() * tapteDagsverk),
                    antallSykemeldinger = antallSykemeldinger,
                    antallPersoner = antallPersoner,
                    tapteDagsverk = tapteDagsverk,
                    muligeDagsverk = muligeDagsverk.toBigDecimal(),
                )
            }.also { generertStatistikkMedGradering[gjeldendeKvartal] = it }
    }
}
