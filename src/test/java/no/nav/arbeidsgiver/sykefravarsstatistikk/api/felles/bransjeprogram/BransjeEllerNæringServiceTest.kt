package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.BransjeEllerNæringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næringskode
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class BransjeEllerNæringServiceTest {
    var bransjeEllerNæringService: BransjeEllerNæringService? = null

    private val barnehage: Næringskode = Næringskode("88911")

    @BeforeEach
    fun setUp() {
        bransjeEllerNæringService = BransjeEllerNæringService()
    }

    @Test
    fun skalHenteDataPåBransjeEllerNæringsnivå_skalReturnereBransje_forBarnehager() {
        val actual = bransjeEllerNæringService!!.bestemFraNæringskode(barnehage)
        assertThat(actual.isBransje).isTrue()
    }

    @Test
    fun skalHenteDataPåBransjeEllerNæringsnivå_skalReturnereNæring_forBedriftINæringsmiddelindustrien() {
        // En bedrift i næringsmiddelindustrien er i bransjeprogrammet, men data hentes likevel på
        // tosiffernivå, aka næringsnivå
        val næringINæringsmiddelindustriBransjen = Næringskode("10411")

        val actual = bransjeEllerNæringService!!.bestemFraNæringskode(næringINæringsmiddelindustriBransjen)
        assertThat(actual.isBransje).isFalse()
        assertThat(actual.næring.tosifferIdentifikator).isEqualTo("10")
    }
}
