package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import ia.felles.definisjoner.bransjer.Bransje
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BransjeEllerNæringTest {
    @Test
    fun BransjeEllerNæring_kan_opprettes_for_en_bransje_og_returnerer_en_bransje() {
        val bransje = Bransje.BARNEHAGER
        val bransjeEllerNæring = BransjeEllerNæring(bransje)
        assertThat(bransjeEllerNæring.bransje).isEqualTo(bransje)
        assertThat(bransjeEllerNæring.næring).isEqualTo(null)
    }

    @Test
    fun BransjeEllerNæring_kan_opprettes_for_en_næring_og_returnerer_en_næring() {
        val næring = Næring("61")
        val bransjeEllerNæring = BransjeEllerNæring(næring)
        assertThat(bransjeEllerNæring.næring).isEqualTo(næring)
        assertThat(bransjeEllerNæring.bransje).isEqualTo(null)
    }
}
