package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import io.vavr.control.Either
import lombok.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.StatistikkException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.UtilstrekkeligDataException
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.Konstanter
import java.math.BigDecimal
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.max

@ToString
@EqualsAndHashCode
@AllArgsConstructor
class SumAvSykefraværOverFlereKvartaler(umaskertSykefravær: UmaskertSykefraværForEttKvartal) {
    @Getter
    var muligeDagsverk: BigDecimal

    @Getter
    var tapteDagsverk: BigDecimal
    private val høyesteAntallPersonerIEtKvartal: Int
    private val kvartaler: List<ÅrstallOgKvartal?>
    private val umaskertSykefraværList: List<UmaskertSykefraværForEttKvartal>

    init {
        muligeDagsverk = umaskertSykefravær.dagsverkNevner
        tapteDagsverk = umaskertSykefravær.dagsverkTeller
        høyesteAntallPersonerIEtKvartal = umaskertSykefravær.antallPersoner
        kvartaler = listOf(umaskertSykefravær.årstallOgKvartal)
        umaskertSykefraværList = listOf(umaskertSykefravær)
    }

    fun regnUtProsentOgMapTilDto(
        type: Statistikkategori, label: String
    ): Either<StatistikkException, StatistikkDto> {
        return kalkulerFraværsprosentMedMaskering()
            .map { prosent: BigDecimal -> tilStatistikkDto(type, label, prosent.toString()) }
    }

    fun regnUtProsentOgMapTilSykefraværForFlereKvartaler(): Either<StatistikkException, SykefraværOverFlereKvartaler> {
        if (muligeDagsverk.compareTo(BigDecimal.ZERO) == 0) {
            return Either.left(
                UtilstrekkeligDataException(
                    "Kan ikke regne ut sykefraværsprosent når antall mulige dagsverk er null."
                )
            )
        }
        val prosent = StatistikkUtils.kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk)
        if (prosent.isLeft) {
            return Either.left(prosent.left)
        }
        val sykefraværForFlereKvartaler = SykefraværOverFlereKvartaler(
            kvartaler,
            tapteDagsverk,
            muligeDagsverk,
            umaskertSykefraværList.stream()
                .map<SykefraværForEttKvartal?> { sf: UmaskertSykefraværForEttKvartal ->
                    SykefraværForEttKvartal(
                        sf.getÅrstallOgKvartal(),
                        sf.getDagsverkTeller(),
                        sf.getDagsverkNevner(),
                        sf.getAntallPersoner()
                    )
                }
                .collect(Collectors.toList<SykefraværForEttKvartal?>()))
        return Either.right(sykefraværForFlereKvartaler)
    }

    private fun kalkulerFraværsprosentMedMaskering(): Either<StatistikkException, BigDecimal> {
        if (datagrunnlagetErTomt()) {
            return Either.left(UtilstrekkeligDataException())
        }
        if (dataMåMaskeres()) {
            return Either.left(MaskerteDataException())
        }
        return if (muligeDagsverk.compareTo(BigDecimal.ZERO) == 0) {
            Either.left(
                UtilstrekkeligDataException(
                    "Kan ikke regne ut sykefraværsprosent når antall mulige dagsverk er null."
                )
            )
        } else StatistikkUtils.kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk)
    }

    fun getTapteDagsverkOgMapTilDto(
        type: Statistikkategori, virksomhetsnavn: String
    ): Either<StatistikkException, StatistikkDto> {
        return getAntallDagsverkOgMapTilDto(type, virksomhetsnavn, this::tapteDagsverk)
    }

    fun getMuligeDagsverkOgMapTilDto(
        type: Statistikkategori, virksomhetsnavn: String
    ): Either<StatistikkException, StatistikkDto> {
        return getAntallDagsverkOgMapTilDto(type, virksomhetsnavn, this::muligeDagsverk)
    }

    fun leggSammen(other: SumAvSykefraværOverFlereKvartaler): SumAvSykefraværOverFlereKvartaler {
        return SumAvSykefraværOverFlereKvartaler(
            muligeDagsverk.add(other.muligeDagsverk),
            tapteDagsverk.add(other.tapteDagsverk),
            max(høyesteAntallPersonerIEtKvartal.toDouble(), other.høyesteAntallPersonerIEtKvartal.toDouble()),
            Stream.concat(kvartaler.stream(), other.kvartaler.stream())
                .distinct()
                .collect(Collectors.toList<Any?>()),
            Stream.concat(umaskertSykefraværList.stream(), other.umaskertSykefraværList.stream())
                .distinct()
                .collect(Collectors.toList<Any>())
        )
    }

    private fun getAntallDagsverkOgMapTilDto(
        type: Statistikkategori,
        virksomhetsnavn: String,
        tapteEllerMuligeDagsverk: Supplier<BigDecimal>
    ): Either<StatistikkException, StatistikkDto> {
        if (datagrunnlagetErTomt()) {
            return Either.left(UtilstrekkeligDataException())
        }
        return if (dataMåMaskeres()) {
            Either.left(MaskerteDataException())
        } else Either.right(
            tilStatistikkDto(type, virksomhetsnavn, tapteEllerMuligeDagsverk.get().toString())
        )
    }

    private fun datagrunnlagetErTomt(): Boolean {
        return this == NULLPUNKT
    }

    private fun dataMåMaskeres(): Boolean {
        return (høyesteAntallPersonerIEtKvartal
                < Konstanter.MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER)
    }

    private fun tilStatistikkDto(type: Statistikkategori, label: String, verdi: String): StatistikkDto {
        return StatistikkDto(type, label, verdi, høyesteAntallPersonerIEtKvartal, kvartaler)
    }

    class MaskerteDataException : StatistikkException("Ikke nok personer i datagrunnlaget - data maskeres.")
    companion object {
        var NULLPUNKT =
            SumAvSykefraværOverFlereKvartaler(BigDecimal.ZERO, BigDecimal.ZERO, 0, listOf<Any>(), listOf<Any>())
    }
}
