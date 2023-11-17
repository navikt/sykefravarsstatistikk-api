package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusPubliseringsdatoerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class PubliseringsdatoerImportServiceTest {
    private var serviceUnderTest: PubliseringsdatoerImportService? = null

    @Mock
    private val mockPubliseringsdatoerRepository: PubliseringsdatoerRepository? = null

    @Mock
    private val mockDatavarehusPubliseringsdatoerRepository: DatavarehusPubliseringsdatoerRepository? = null

    @BeforeEach
    fun setUp() {
        serviceUnderTest = PubliseringsdatoerImportService(
            mockPubliseringsdatoerRepository!!, mockDatavarehusPubliseringsdatoerRepository!!
        )
    }

    @AfterEach
    fun tearDown() {
        Mockito.reset(mockPubliseringsdatoerRepository, mockDatavarehusPubliseringsdatoerRepository)
    }

    @Test
    fun importerDatoerFraDatavarehus_oppdaterPubliseringsdatoerBlirKjørtEnGang() {
        val publiseringsdatoListe = listOf(
            Publiseringsdato(
                202202,
                LocalDate.MIN,
                LocalDate.MIN,
                "sykefravær for en periode"
            )
        )
        Mockito.`when`(mockDatavarehusPubliseringsdatoerRepository!!.hentPubliseringsdatoerFraDvh())
            .thenReturn(publiseringsdatoListe)
        serviceUnderTest!!.importerDatoerFraDatavarehus()
        Mockito.verify(mockPubliseringsdatoerRepository, Mockito.times(1))
            ?.oppdaterPubliseringsdatoer(publiseringsdatoListe)
    }
}
