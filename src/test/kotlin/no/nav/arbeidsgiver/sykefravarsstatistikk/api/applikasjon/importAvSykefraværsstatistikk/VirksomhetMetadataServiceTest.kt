package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.KildeTilVirksomhetsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.VirksomhetMetadataService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.VirksomhetMetadataService.IngenRaderImportert
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VirksomhetMetadataRepository
import org.junit.jupiter.api.Test

internal class VirksomhetMetadataServiceTest {
    private val virksomhetMetadataRepository: VirksomhetMetadataRepository = mockk()
    private val kildeTilVirksomhetsdata: KildeTilVirksomhetsdata = mockk()

    val service = VirksomhetMetadataService(
        kildeTilVirksomhetsdata,
        virksomhetMetadataRepository
    )

    @Test
    fun `overskrivMetadataForVirksomheter skal slette gammel data og deretter dytte inn ny`() {

        val dummyKvartal = ÅrstallOgKvartal(
            årstall = 2023, kvartal = 1
        )

        val dummyvirksomheter = listOf(
            Orgenhet(
                orgnr = Orgnr("111111111"),
                navn = "navn",
                rectype = "2",
                sektor = Sektor.STATLIG,
                næring = "10",
                næringskode = "10123",
                årstallOgKvartal = dummyKvartal
            )
        )

        every { kildeTilVirksomhetsdata.hentVirksomheter(any()) } returns dummyvirksomheter

        every { virksomhetMetadataRepository.slettVirksomhetMetadata() } returns 1
        every { virksomhetMetadataRepository.opprettVirksomhetMetadata(any()) } returns 1

        val antallOpprettet = service.overskrivMetadataForVirksomheter(årstallOgKvartal = dummyKvartal)


        antallOpprettet shouldBeEqual 1.right()
        verify(exactly = 1) { virksomhetMetadataRepository.slettVirksomhetMetadata() }
        verify(exactly = 1) { virksomhetMetadataRepository.opprettVirksomhetMetadata(dummyvirksomheter.map { it.tilDomene() }) }
    }

    @Test
    fun `overskrivMetadataForVirksomheter skal ikke slette data dersom ingen virksomheter blir funnet`() {

        val dummyKvartal = ÅrstallOgKvartal(
            årstall = 2023, kvartal = 1
        )

        every { kildeTilVirksomhetsdata.hentVirksomheter(any()) } returns emptyList()

        val resultat = service.overskrivMetadataForVirksomheter(årstallOgKvartal = dummyKvartal)


        resultat shouldBeEqual IngenRaderImportert.left()
        verify(exactly = 0) { virksomhetMetadataRepository.slettVirksomhetMetadata() }
        verify(exactly = 0) { virksomhetMetadataRepository.opprettVirksomhetMetadata(any()) }
    }
}
