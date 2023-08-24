package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.BransjeEllerNæringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.BedreNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.VirksomhetMetadata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.ORGNR_VIRKSOMHET_1
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.ORGNR_VIRKSOMHET_2
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KlassifikasjonerRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class BransjeEllerNæringServiceTest {
    var bransjeEllerNæringService: BransjeEllerNæringService? = null

    private val klassifikasjonerRepository: KlassifikasjonerRepository = mock()

    private val barnehage: BedreNæringskode = BedreNæringskode("88911")
    private val virksomhetMetadata = VirksomhetMetadata(
        ORGNR_VIRKSOMHET_1,
        "Virksomhet 1",
        DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
        Sektor.STATLIG,
        "88",
        "88000",
        TestUtils.SISTE_PUBLISERTE_KVARTAL
    )

    @BeforeEach
    fun setUp() {
        bransjeEllerNæringService = BransjeEllerNæringService(klassifikasjonerRepository)
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
        val næringINæringsmiddelindustriBransjen = BedreNæringskode("10411")
        whenever(klassifikasjonerRepository.hentNæring(any())).thenReturn(Næring("10411", "test"))

        val actual = bransjeEllerNæringService!!.bestemFraNæringskode(næringINæringsmiddelindustriBransjen)
        assertThat(actual.isBransje).isFalse()
        assertThat(actual.næring.kode).isEqualTo("10411")
    }

    @Test
    fun finnBransejFraMetadata__skalFinneRiktigBransjeFraMetadata() {
        virksomhetMetadata.leggTilNæringOgNæringskode5siffer(
            listOf(
                barnehage,
                BedreNæringskode("00000"),
            )
        )
        val resultat = bransjeEllerNæringService!!.finnBransjeFraMetadata(virksomhetMetadata, listOf())
        Assertions.assertTrue(resultat.isBransje)
        assertThat(resultat.getBransje().type).isEqualTo(ArbeidsmiljøportalenBransje.BARNEHAGER)
        assertThat(resultat.getBransje().koderSomSpesifisererNæringer)
            .isEqualTo(listOf("88911"))
    }

    @Test
    fun finnBransejFraMetadata__skalIkkeFeileVedManglendeAvNæringskode5sifferListe() {
        val resultat = bransjeEllerNæringService!!.finnBransjeFraMetadata(virksomhetMetadata, listOf())
        Assertions.assertFalse(resultat.isBransje)
        assertThat(resultat.næring.kode).isEqualTo("88")
        assertThat(resultat.næring.navn).isEqualTo("Ukjent næring")
    }

    @Test
    fun finnBransejFraMetadata__skalReturnereRiktigNæringsbeskrivelse() {
        val virksomhetMetadata2 = VirksomhetMetadata(
            ORGNR_VIRKSOMHET_2,
            "Virksomhet 2",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            Sektor.STATLIG,
            "11",
            "11000",
            TestUtils.SISTE_PUBLISERTE_KVARTAL
        )
        val resultat = bransjeEllerNæringService!!.finnBransjeFraMetadata(
            virksomhetMetadata2,
            listOf(
                Næring("02", "Skogbruk og tjenester tilknyttet skogbruk"),
                Næring("11", "Produksjon av drikkevarer")
            )
        )
        Assertions.assertFalse(resultat.isBransje)
        assertThat(resultat.næring.kode).isEqualTo("11")
        assertThat(resultat.næring.navn).isEqualTo("Produksjon av drikkevarer")
    }
}
