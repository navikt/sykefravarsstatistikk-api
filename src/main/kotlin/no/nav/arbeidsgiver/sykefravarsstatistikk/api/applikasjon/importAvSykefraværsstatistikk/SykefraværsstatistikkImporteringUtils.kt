package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori.TOTAL
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhetMedGradering
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import java.math.BigDecimal.ZERO
import kotlin.time.times

object SykefraværsstatistikkImporteringUtils {
    private val generertStatistikk = mutableMapOf<ÅrstallOgKvartal, List<SykefraværsstatistikkVirksomhet>>()
    private val generertStatistikkMedGradering =
        mutableMapOf<ÅrstallOgKvartal, List<SykefraværsstatistikkVirksomhetMedGradering>>()

    fun genererSykefraværsstatistikkVirksomhet(
        gjeldendeKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkVirksomhet> {
        generertStatistikk[gjeldendeKvartal]?.let {
            return it
        }

        generertStatistikk[gjeldendeKvartal] = HardkodetKildeTilVirksomhetsdata.hentVirksomheter(gjeldendeKvartal).flatMap {
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
                    tapteDagsverk = if (varighet == TOTAL) ZERO else tapteDagsverk.toBigDecimal(),
                    muligeDagsverk = if (varighet == TOTAL) muligeDagsverk.toBigDecimal() else ZERO,
                )
            }
        }

        return generertStatistikk[gjeldendeKvartal]!!
    }

    fun genererSykefraværsstatistikkVirksomhetMedGradering(gjeldendeKvartal: ÅrstallOgKvartal): List<SykefraværsstatistikkVirksomhetMedGradering> {
        generertStatistikkMedGradering[gjeldendeKvartal]?.let {
            return it
        }

        generertStatistikkMedGradering[gjeldendeKvartal] = genererSykefraværsstatistikkVirksomhet(gjeldendeKvartal)
            .groupBy { it.orgnr!! }
            .map { (orgnr, value) ->
                val muligeDagsverk = value.sumOf { it.muligeDagsverk }
                val tapteDagsverk = value.sumOf { it.tapteDagsverk }
                val antallPersoner = value.sumOf { it.antallPersoner }

                val graderingsFaktor = Math.random()
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = gjeldendeKvartal.årstall,
                    kvartal = gjeldendeKvartal.kvartal,
                    orgnr = orgnr,
                    næring = value.first().,
                    næringkode = "",
                    rectype = "",
                    antallGraderteSykemeldinger = (graderingsFaktor * antallPersoner).toInt(),
                    tapteDagsverkGradertSykemelding = (graderingsFaktor.toBigDecimal() * tapteDagsverk),
                    antallSykemeldinger = 0,
                    antallPersoner = antallPersoner,
                    tapteDagsverk = tapteDagsverk,
                    muligeDagsverk = muligeDagsverk,
                )
            }
    }
}
