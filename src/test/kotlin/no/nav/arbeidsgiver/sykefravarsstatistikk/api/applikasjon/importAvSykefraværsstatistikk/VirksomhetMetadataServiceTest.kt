package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.KildeTilVirksomhetsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VirksomhetMetadataRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class VirksomhetMetadataServiceTest {
    private val datavarehusRepository: KildeTilVirksomhetsdata = mock()

    private val virksomhetMetadataRepository: VirksomhetMetadataRepository = mock()

    @Test
    fun `Tester at import av metadata går som vi forventer`() {
        TODO()
    }
}
