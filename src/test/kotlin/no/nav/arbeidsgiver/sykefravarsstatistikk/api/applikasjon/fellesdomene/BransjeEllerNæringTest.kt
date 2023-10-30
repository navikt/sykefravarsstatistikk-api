package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import ia.felles.definisjoner.bransjer.Bransjer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class BransjeEllerNæringTest {
    @Test
    fun BransjeEllerNæring_kan_opprettes_for_en_bransje_og_returnerer_en_bransje() {
        val bransje = Bransje(Bransjer.BARNEHAGER)
        val bransjeEllerNæring = BransjeEllerNæring(bransje)
        assertThat(bransjeEllerNæring.isBransje).isEqualTo(true)
        assertThat(bransjeEllerNæring.getBransje()).isEqualTo(bransje)
        assertThrows(NoSuchElementException::class.java) { bransjeEllerNæring.næring }
    }

    @Test
    fun BransjeEllerNæring_kan_opprettes_for_en_næring_og_returnerer_en_næring() {
        val næring = Næring("61")
        val bransjeEllerNæring = BransjeEllerNæring(næring)
        assertThat(bransjeEllerNæring.isBransje).isEqualTo(false)
        assertThat(bransjeEllerNæring.næring).isEqualTo(næring)
        assertThrows(NoSuchElementException::class.java) { bransjeEllerNæring.getBransje() }
    }
}
