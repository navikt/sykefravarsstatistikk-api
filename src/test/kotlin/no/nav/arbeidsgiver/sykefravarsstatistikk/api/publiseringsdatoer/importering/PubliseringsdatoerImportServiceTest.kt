package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.importering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi.PubliseringsdatoerImportService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi.Publiseringsdato
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.sql.Date
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class PubliseringsdatoerImportServiceTest {
    private var serviceUnderTest: PubliseringsdatoerImportService? = null

    @Mock
    private val mockPubliseringsdatoerRepository: PubliseringsdatoerRepository? = null

    @Mock
    private val mockDatavarehusRepository: DatavarehusRepository? = null

    @BeforeEach
    fun setUp() {
        serviceUnderTest = PubliseringsdatoerImportService(
            mockPubliseringsdatoerRepository!!, mockDatavarehusRepository!!
        )
    }

    @AfterEach
    fun tearDown() {
        Mockito.reset(mockPubliseringsdatoerRepository, mockDatavarehusRepository)
    }

    @Test
    fun importerDatoerFraDatavarehus_oppdaterPubliseringsdatoerBlirKjørtEnGang() {
        val publiseringsdatoListe = listOf(
            Publiseringsdato(
                202202,
                Date.valueOf(LocalDate.MIN),
                Date.valueOf(LocalDate.MIN),
                "sykefravær for en periode"
            )
        )
        Mockito.`when`(mockDatavarehusRepository!!.hentPubliseringsdatoerFraDvh())
            .thenReturn(publiseringsdatoListe)
        serviceUnderTest!!.importerDatoerFraDatavarehus()
        Mockito.verify(mockPubliseringsdatoerRepository, Mockito.times(1))
            ?.oppdaterPubliseringsdatoer(publiseringsdatoListe)
    }
}
