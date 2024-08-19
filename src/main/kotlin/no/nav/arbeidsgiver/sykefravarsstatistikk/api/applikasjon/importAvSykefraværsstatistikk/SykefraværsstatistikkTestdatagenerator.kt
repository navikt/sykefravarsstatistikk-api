package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori.TOTAL
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhetMedGradering
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.HardkodetKildeTilVirksomhetsdata.Testvirksomhet.VIRKSOMHETSSTØRRELSE
import java.math.BigDecimal.ZERO


/**
 * Denne klassen er anvarlig for å generere opp testdata til bruk i dev-miljøene. Den genererte statistikken persisteres
 * i en map, slik at kan oppnå en viss kontinuitet mellom totalsykefraværet og det graderte fraværet.
 */
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
            .map { (virksomhet, størrelse) ->
                val antallPersoner = when (størrelse) {
                    VIRKSOMHETSSTØRRELSE.KNØTT -> (0..6)
                    VIRKSOMHETSSTØRRELSE.LITEN -> (5..10)
                    VIRKSOMHETSSTØRRELSE.MEDIUM -> (20..30)
                    VIRKSOMHETSSTØRRELSE.STOR -> (75..100)
                    VIRKSOMHETSSTØRRELSE.ENORM -> (11_000..12_000)
                }.random()

                val antallDagsverkIEttKvartal = 230 / 4
                val muligeDagsverk = antallPersoner * antallDagsverkIEttKvartal

                val tapteDagsverk = (0..muligeDagsverk).random().toBigDecimal()

                val antallSykemeldinger = (0..(antallPersoner * 1.25).toInt()).random()

                val graderingsfaktor = Math.random()

                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = gjeldendeKvartal.årstall,
                    kvartal = gjeldendeKvartal.kvartal,
                    orgnr = virksomhet.orgnr.verdi,
                    næring = virksomhet.næring!!,
                    næringkode = virksomhet.næringskode!!,
                    rectype = virksomhet.rectype!!,
                    tapteDagsverkGradertSykemelding = (graderingsfaktor.toBigDecimal() * tapteDagsverk),
                    antallPersoner = antallPersoner,
                    tapteDagsverk = tapteDagsverk,
                    muligeDagsverk = muligeDagsverk.toBigDecimal(),
                )
            }.also { generertStatistikkMedGradering[gjeldendeKvartal] = it }
    }
}
