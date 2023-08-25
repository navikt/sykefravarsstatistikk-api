package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PostImporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.GraderingRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaUtsendingHistorikkRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_1;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.kotlin.OngoingStubbingKt.whenever;

@ExtendWith(MockitoExtension.class)
class PostImporteringServiceTest {

    @Mock
    private DatavarehusRepository datavarehusRepository;
    @Mock
    private VirksomhetMetadataRepository virksomhetMetadataRepository;
    @Mock
    private GraderingRepository graderingRepository;
    @Mock
    private EksporteringRepository eksporteringRepository;
    @Mock
    private KafkaUtsendingHistorikkRepository kafkaUtsendingHistorikkRepository;

    private PostImporteringService service;
    private ÅrstallOgKvartal __2020_4 = new ÅrstallOgKvartal(2020, 4);

    @BeforeEach
    public void setUp() {
        service =
                new PostImporteringService(
                        datavarehusRepository,
                        virksomhetMetadataRepository,
                        graderingRepository,
                        eksporteringRepository,
                        kafkaUtsendingHistorikkRepository
                );
    }

    @Test
    public void
    fullførPostImporteringOgForberedNesteEksport__returnerer_antall_virksomheter_som_skal_til_neste_eksport() {
        mockImportVirksomhetMetadata(__2020_4, getOrgenhetListe(__2020_4));
        mockImportVirksomhetNæringskode5sifferMapping(
                __2020_4, getVirksomhetMetadataNæringskode5sifferListe(__2020_4));
        mockForberedNesteEksport(__2020_4, getVirksomhetMetadataListe(__2020_4));

        service.overskrivMetadataForVirksomheter(__2020_4);
        service.overskrivNæringskoderForVirksomheter(__2020_4);
        Integer antall = service.forberedNesteEksport(__2020_4, true).getOrNull();
        assertEquals(2, antall);
    }


    private void mockForberedNesteEksport(
            ÅrstallOgKvartal årstallOgKvartal, List<VirksomhetMetadata> virksomhetMetadataListe) {
        when(virksomhetMetadataRepository.hentVirksomhetMetadataMedNæringskoder(årstallOgKvartal))
                .thenReturn(virksomhetMetadataListe);
        when(eksporteringRepository.opprettEksport(any())).thenReturn(virksomhetMetadataListe.size());
    }

    private void mockImportVirksomhetMetadata(
            ÅrstallOgKvartal årstallOgKvartal, List<Orgenhet> orgenhetSomSkalTilVirksomhetMetadata) {
        when(datavarehusRepository.hentVirksomheter(årstallOgKvartal))
                .thenReturn(orgenhetSomSkalTilVirksomhetMetadata);
        when(virksomhetMetadataRepository.opprettVirksomhetMetadata(any()))
                .thenReturn(orgenhetSomSkalTilVirksomhetMetadata.size());
    }

    private void mockImportVirksomhetNæringskode5sifferMapping(
            ÅrstallOgKvartal årstallOgKvartal,
            List<VirksomhetMetadataMedNæringskode> virksomhetMetadataMedNæringskodeListe) {
        when(graderingRepository.hentVirksomhetMetadataNæringskode5siffer(årstallOgKvartal))
                .thenReturn(virksomhetMetadataMedNæringskodeListe);
        whenever(virksomhetMetadataRepository.opprettVirksomhetMetadataNæringskode5siffer(any()))
                .thenReturn(virksomhetMetadataMedNæringskodeListe.size());
    }

    private List<VirksomhetMetadataMedNæringskode> getVirksomhetMetadataNæringskode5sifferListe(
            ÅrstallOgKvartal årstallOgKvartal) {
        List<VirksomhetMetadataMedNæringskode> virksomhetMetadataMedNæringskode =
                new ArrayList<>();
        virksomhetMetadataMedNæringskode.add(
                new VirksomhetMetadataMedNæringskode(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        årstallOgKvartal,
                        new Næringskode("10101")
                ));
        virksomhetMetadataMedNæringskode.add(
                new VirksomhetMetadataMedNæringskode(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        årstallOgKvartal,
                        new Næringskode("10102")
                ));
        virksomhetMetadataMedNæringskode.add(
                new VirksomhetMetadataMedNæringskode(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        årstallOgKvartal,
                        new Næringskode("20101")
                ));

        return virksomhetMetadataMedNæringskode;
    }

    @NotNull
    private List<Orgenhet> getOrgenhetListe(ÅrstallOgKvartal årstallOgKvartal) {
        List<Orgenhet> orgenhetSomSkalTilVirksomhetMetadata = new ArrayList<>();
        orgenhetSomSkalTilVirksomhetMetadata.add(
                new Orgenhet(
                        new Orgnr(ORGNR_VIRKSOMHET_1), "Virksomhet 1", "2", Sektor.PRIVAT, "10", "10000", årstallOgKvartal));
        orgenhetSomSkalTilVirksomhetMetadata.add(
                new Orgenhet(
                        new Orgnr(ORGNR_VIRKSOMHET_2), "Virksomhet 2", "2", Sektor.PRIVAT, "20", "20000", årstallOgKvartal));

        return orgenhetSomSkalTilVirksomhetMetadata;
    }

    private List<VirksomhetMetadata> getVirksomhetMetadataListe(ÅrstallOgKvartal årstallOgKvartal) {
        List<VirksomhetMetadata> virksomhetMetadataListe = new ArrayList<>();
        virksomhetMetadataListe.add(
                new VirksomhetMetadata(
                        new Orgnr(ORGNR_VIRKSOMHET_1), "Virksomhet 1", "2", Sektor.PRIVAT, "10", "10000", årstallOgKvartal));
        virksomhetMetadataListe.add(
                new VirksomhetMetadata(
                        new Orgnr(ORGNR_VIRKSOMHET_2), "Virksomhet 2", "2", Sektor.PRIVAT, "20", "20000", årstallOgKvartal));

        return virksomhetMetadataListe;
    }
}
