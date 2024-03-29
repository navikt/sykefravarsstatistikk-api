package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Importtidspunkt
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImporttidspunktRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class PubliseringsdatoerServiceTest {
    private val publiseringsdatoer = listOf(
        Publiseringsdato(
            202201,
            LocalDate.parse("2022-06-02"),
            LocalDate.parse("2021-11-01"),
        ),
        Publiseringsdato(
            202104,
            LocalDate.parse("2022-03-03"),
            LocalDate.parse("2021-11-01"),
        ),
        Publiseringsdato(
            202203,
            LocalDate.parse("2022-12-01"),
            LocalDate.parse("2021-11-01"),
        ),
        Publiseringsdato(
            202202,
            LocalDate.parse("2022-09-08"),
            LocalDate.parse("2021-11-01"),
        )
    )
    private var serviceUnderTest: PubliseringsdatoerService? = null

    @Mock
    private val mockPubliseringsdatoerRepository: PubliseringsdatoerRepository? = null

    @Mock
    private val mockImporttidspunktRepository: ImporttidspunktRepository? = null

    @BeforeEach
    fun setUp() {
        serviceUnderTest = PubliseringsdatoerService(
            mockPubliseringsdatoerRepository!!,
            mockImporttidspunktRepository!!
        )
    }

    @AfterEach
    fun tearDown() {
        Mockito.reset(mockPubliseringsdatoerRepository)
    }

    @Test
    fun hentPubliseringsdatoer_nårPubliseringHarSkjeddSomPlanlagt_publiseringsdatoerErRiktige() {
        val sisteImporttidspunkt = Importtidspunkt(
            LocalDate.of(2022, 9, 8),
            ÅrstallOgKvartal(2022, 2)
        )
        Mockito.`when`(mockImporttidspunktRepository?.hentNyesteImporterteKvartal())
            .thenReturn(sisteImporttidspunkt)
        Mockito.`when`(mockPubliseringsdatoerRepository?.hentPubliseringsdatoer()).thenReturn(publiseringsdatoer)
        val faktiskeDatoer = serviceUnderTest!!.hentPubliseringsdatoer()

        val forventet = PubliseringskalenderDto(
            LocalDate.parse("2022-09-08"),
            ÅrstallOgKvartal(2022, 2),
            LocalDate.parse("2022-12-01")
        )

        Assertions.assertThat(faktiskeDatoer).isEqualTo(forventet)
    }

    @Test
    fun hentPubliseringsdatoer_nårPubliseringSkjerEnDagForSent_returnererKorrektDatoForForrigePubliseringIstedenforPlanlagtDato() {
        val enDagEtterPlanlagtImport = Importtidspunkt(
            LocalDate.of(2022, 6, 3),
            ÅrstallOgKvartal(2022, 1)
        )
        Mockito.`when`(mockImporttidspunktRepository?.hentNyesteImporterteKvartal())
            .thenReturn(enDagEtterPlanlagtImport)
        Mockito.`when`(mockPubliseringsdatoerRepository?.hentPubliseringsdatoer()).thenReturn(publiseringsdatoer)
        val faktiskeDatoer = serviceUnderTest!!.hentPubliseringsdatoer()
        val forventet = PubliseringskalenderDto(
            LocalDate.parse("2022-06-03"),
            ÅrstallOgKvartal(2022, 1),
            LocalDate.parse("2022-09-08")
        )
        Assertions.assertThat(faktiskeDatoer).isEqualTo(forventet)
    }
}
