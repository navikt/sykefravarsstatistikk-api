package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import testUtils.TestData.ORGNR_VIRKSOMHET_1
import testUtils.TestData.ORGNR_VIRKSOMHET_2
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.VirksomhetMetadataService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetMetadata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetMetadataMedNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Næringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.EksporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.GraderingRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VirksomhetMetadataRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaUtsendingHistorikkRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class VirksomhetMetadataServiceTest {
    private val datavarehusRepository: DatavarehusRepository = mock()

    private val virksomhetMetadataRepository: VirksomhetMetadataRepository = mock()

    private val graderingRepository: GraderingRepository = mock()

    private val eksporteringRepository: EksporteringRepository = mock()

    private val kafkaUtsendingHistorikkRepository: KafkaUtsendingHistorikkRepository = mock()
    private var service: VirksomhetMetadataService = VirksomhetMetadataService(
        datavarehusRepository,
        virksomhetMetadataRepository,
        graderingRepository,
        eksporteringRepository,
        kafkaUtsendingHistorikkRepository
    )
    private val __2020_4 = ÅrstallOgKvartal(2020, 4)

    @Test
    fun fullførPostImporteringOgForberedNesteEksport__returnerer_antall_virksomheter_som_skal_til_neste_eksport() {
        mockImportVirksomhetMetadata(getOrgenhetListe(__2020_4))
        mockImportVirksomhetNæringskode5sifferMapping(
            getVirksomhetMetadataNæringskode5sifferListe(__2020_4)
        )
        mockForberedNesteEksport(__2020_4, getVirksomhetMetadataListe(__2020_4))
        service.overskrivMetadataForVirksomheter(__2020_4)
        service.overskrivNæringskoderForVirksomheter(__2020_4)
        val antall = service.forberedNesteEksport(__2020_4, true).getOrNull()
        Assertions.assertEquals(2, antall)
    }

    private fun mockForberedNesteEksport(
        årstallOgKvartal: ÅrstallOgKvartal, virksomhetMetadataListe: List<VirksomhetMetadata>
    ) {
        whenever(virksomhetMetadataRepository.hentVirksomhetMetadataMedNæringskoder(årstallOgKvartal))
            .thenReturn(virksomhetMetadataListe)
        whenever(eksporteringRepository.opprettEksport(ArgumentMatchers.any()))
            .thenReturn(virksomhetMetadataListe.size)
    }

    private fun mockImportVirksomhetMetadata(
        orgenhetSomSkalTilVirksomhetMetadata: List<Orgenhet>
    ) {
        whenever(datavarehusRepository.hentVirksomheter(any()))
            .thenReturn(orgenhetSomSkalTilVirksomhetMetadata)
        whenever(virksomhetMetadataRepository.opprettVirksomhetMetadata(any()))
            .thenReturn(orgenhetSomSkalTilVirksomhetMetadata.size)
    }

    private fun mockImportVirksomhetNæringskode5sifferMapping(
        virksomhetMetadataMedNæringskodeListe: List<VirksomhetMetadataMedNæringskode>
    ) {
        whenever(graderingRepository.hentVirksomhetMetadataNæringskode5siffer(any()))
            .thenReturn(virksomhetMetadataMedNæringskodeListe)
        whenever(virksomhetMetadataRepository.opprettVirksomhetMetadataNæringskode5siffer(any()))
            .thenReturn(virksomhetMetadataMedNæringskodeListe.size)
    }

    private fun getVirksomhetMetadataNæringskode5sifferListe(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<VirksomhetMetadataMedNæringskode> {
        val virksomhetMetadataMedNæringskode: MutableList<VirksomhetMetadataMedNæringskode> = ArrayList()
        virksomhetMetadataMedNæringskode.add(
            VirksomhetMetadataMedNæringskode(
                Orgnr(ORGNR_VIRKSOMHET_1),
                årstallOgKvartal,
                Næringskode("10101")
            )
        )
        virksomhetMetadataMedNæringskode.add(
            VirksomhetMetadataMedNæringskode(
                Orgnr(ORGNR_VIRKSOMHET_1),
                årstallOgKvartal,
                Næringskode("10102")
            )
        )
        virksomhetMetadataMedNæringskode.add(
            VirksomhetMetadataMedNæringskode(
                Orgnr(ORGNR_VIRKSOMHET_1),
                årstallOgKvartal,
                Næringskode("20101")
            )
        )
        return virksomhetMetadataMedNæringskode
    }

    private fun getOrgenhetListe(årstallOgKvartal: ÅrstallOgKvartal): List<Orgenhet> {
        val orgenhetSomSkalTilVirksomhetMetadata: MutableList<Orgenhet> = ArrayList()
        orgenhetSomSkalTilVirksomhetMetadata.add(
            Orgenhet(
                Orgnr(ORGNR_VIRKSOMHET_1), "Virksomhet 1", "2", Sektor.PRIVAT, "10", "10000", årstallOgKvartal
            )
        )
        orgenhetSomSkalTilVirksomhetMetadata.add(
            Orgenhet(
                Orgnr(ORGNR_VIRKSOMHET_2), "Virksomhet 2", "2", Sektor.PRIVAT, "20", "20000", årstallOgKvartal
            )
        )
        return orgenhetSomSkalTilVirksomhetMetadata
    }

    private fun getVirksomhetMetadataListe(årstallOgKvartal: ÅrstallOgKvartal): List<VirksomhetMetadata> {
        val virksomhetMetadataListe: MutableList<VirksomhetMetadata> = ArrayList()
        virksomhetMetadataListe.add(
            VirksomhetMetadata(
                Orgnr(ORGNR_VIRKSOMHET_1), "Virksomhet 1", "2", Sektor.PRIVAT, "10", "10000", årstallOgKvartal
            )
        )
        virksomhetMetadataListe.add(
            VirksomhetMetadata(
                Orgnr(ORGNR_VIRKSOMHET_2), "Virksomhet 2", "2", Sektor.PRIVAT, "20", "20000", årstallOgKvartal
            )
        )
        return virksomhetMetadataListe
    }
}
