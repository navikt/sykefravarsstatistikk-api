package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.BedreNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Virksomhet
import java.util.*

object Bransjeprogram {
    private val BARNEHAGER = Bransje(ArbeidsmiljøportalenBransje.BARNEHAGER, "Barnehager", listOf("88911"))
    private val NÆRINGSMIDDELINDUSTRI = Bransje(
        ArbeidsmiljøportalenBransje.NÆRINGSMIDDELINDUSTRI, "Næringsmiddelsindustrien", listOf("10")
    )
    private val SYKEHUS = Bransje(
        ArbeidsmiljøportalenBransje.SYKEHUS,
        "Sykehus",
        listOf(
            "86101",
            "86102",
            "86104",
            "86105",
            "86106",
            "86107"
        )
    )
    private val SYKEHJEM = Bransje(ArbeidsmiljøportalenBransje.SYKEHJEM, "Sykehjem", listOf("87101", "87102"))
    private val TRANSPORT = Bransje(
        ArbeidsmiljøportalenBransje.TRANSPORT,
        "Rutebuss og persontrafikk (transport)",
        listOf(
            "49100",
            "49311",
            "49391",
            "49392"
        )
    )
    private val BYGG = Bransje(ArbeidsmiljøportalenBransje.BYGG, "Bygg", listOf("41"))
    private val ANLEGG = Bransje(ArbeidsmiljøportalenBransje.ANLEGG, "Anlegg", listOf("42"))

    val alleBransjer: List<Bransje> =
        listOf(BARNEHAGER, NÆRINGSMIDDELINDUSTRI, SYKEHUS, SYKEHJEM, TRANSPORT, BYGG, ANLEGG)

    @JvmStatic
    fun finnBransje(underenhet: Virksomhet?): Optional<Bransje> = finnBransje(underenhet?.næringskode)

    @JvmStatic
    fun finnBransje(næringskode5Siffer: String?): Optional<Bransje> =
        Optional.ofNullable(alleBransjer.firstOrNull { it.inkludererNæringskode(næringskode5Siffer) })

    @JvmStatic
    fun finnBransje(næringskode5Siffer: BedreNæringskode?): Optional<Bransje> = finnBransje(næringskode5Siffer?.femsifferIdentifikator)
}
