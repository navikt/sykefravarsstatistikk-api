package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet
import org.springframework.stereotype.Component
import java.util.*

@Component
object Bransjeprogram {
    private val BARNEHAGER = Bransje(ArbeidsmiljøportalenBransje.BARNEHAGER, "Barnehager", "88911")
    private val NÆRINGSMIDDELINDUSTRI = Bransje(
        ArbeidsmiljøportalenBransje.NÆRINGSMIDDELINDUSTRI, "Næringsmiddelsindustrien", "10"
    )
    private val SYKEHUS = Bransje(
        ArbeidsmiljøportalenBransje.SYKEHUS,
        "Sykehus",
        "86101",
        "86102",
        "86104",
        "86105",
        "86106",
        "86107"
    )
    private val SYKEHJEM = Bransje(ArbeidsmiljøportalenBransje.SYKEHJEM, "Sykehjem", "87101", "87102")
    private val TRANSPORT = Bransje(
        ArbeidsmiljøportalenBransje.TRANSPORT,
        "Rutebuss og persontrafikk (transport)",
        "49100",
        "49311",
        "49391",
        "49392"
    )
    private val BYGG = Bransje(ArbeidsmiljøportalenBransje.BYGG, "Bygg", "41")
    private val ANLEGG = Bransje(ArbeidsmiljøportalenBransje.ANLEGG, "Anlegg", "42")

    val alleBransjer: List<Bransje> = listOf(BARNEHAGER, NÆRINGSMIDDELINDUSTRI, SYKEHUS, SYKEHJEM, TRANSPORT, BYGG, ANLEGG)

    @JvmStatic
    fun finnBransje(underenhet: Underenhet?): Optional<Bransje> = finnBransje(underenhet?.næringskode)

    fun finnBransje(næringskode5Siffer: String?): Optional<Bransje> =
        Optional.ofNullable(alleBransjer.firstOrNull { it.inkludererNæringskode(næringskode5Siffer) })

    @JvmStatic
    fun finnBransje(næringskode5Siffer: Næringskode5Siffer?): Optional<Bransje> = finnBransje(næringskode5Siffer?.kode)
}
