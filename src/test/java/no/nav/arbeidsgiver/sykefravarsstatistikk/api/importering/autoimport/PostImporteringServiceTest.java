package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.NæringOgNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner.Orgenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.GraderingRepository;
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

    private PostImporteringService service;
    private Kvartal __2020_4 = new Kvartal(2020, 4);

    @BeforeEach
    public void setUp() {
        service = new PostImporteringService(
                datavarehusRepository,
                virksomhetMetadataRepository,
                graderingRepository,
                eksporteringRepository,
                true,
                true
        );
    }

    @Test
    public void fullførPostImporteringOgForberedNesteEksport__returnerer_antall_virksomheter_som_skal_til_neste_eksport() {
        mockImportVirksomhetMetadata(__2020_4, getOrgenhetListe(__2020_4));
        mockImportVirksomhetNæringskode5sifferMapping(__2020_4, getVirksomhetMetadataNæringskode5sifferListe(__2020_4));
        mockForberedNesteEksport(
                __2020_4,
                getVirksomhetMetadataListe(__2020_4)
        );

        int antall = service.fullførPostImporteringOgForberedNesteEksport(__2020_4);

        assertEquals(2, antall);
    }

    private void mockForberedNesteEksport(
            Kvartal kvartal,
            List<VirksomhetMetadata> virksomhetMetadataListe
    ) {
        when(
                virksomhetMetadataRepository.hentVirksomhetMetadata(kvartal))
                .thenReturn(virksomhetMetadataListe);
        when(
                eksporteringRepository.opprettEksport(any()))
                .thenReturn(virksomhetMetadataListe.size());
    }

    private void mockImportVirksomhetMetadata(
            Kvartal kvartal,
            List<Orgenhet> orgenhetSomSkalTilVirksomhetMetadata
    ) {
        when(
                datavarehusRepository.hentOrgenhet(kvartal, true))
                .thenReturn(orgenhetSomSkalTilVirksomhetMetadata);
        when(
                virksomhetMetadataRepository.opprettVirksomhetMetadata(any()))
                .thenReturn(orgenhetSomSkalTilVirksomhetMetadata.size());
    }

    private void mockImportVirksomhetNæringskode5sifferMapping(
            Kvartal kvartal,
            List<VirksomhetMetadataNæringskode5siffer> virksomhetMetadataNæringskode5sifferListe
    ) {
        when(
                graderingRepository.hentVirksomhetMetadataNæringskode5siffer(kvartal))
                .thenReturn(virksomhetMetadataNæringskode5sifferListe);
        when(
                virksomhetMetadataRepository.opprettVirksomhetMetadataNæringskode5siffer(any()))
                .thenReturn(virksomhetMetadataNæringskode5sifferListe.size());
    }

    private List<VirksomhetMetadataNæringskode5siffer> getVirksomhetMetadataNæringskode5sifferListe(
            Kvartal kvartal
    ) {
        List<VirksomhetMetadataNæringskode5siffer> virksomhetMetadataNæringskode5siffer = new ArrayList<>();
        virksomhetMetadataNæringskode5siffer.add(
                new VirksomhetMetadataNæringskode5siffer(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        kvartal,
                        new NæringOgNæringskode5siffer(
                                "10",
                                "10101"
                        )
                ));
        virksomhetMetadataNæringskode5siffer.add(
                new VirksomhetMetadataNæringskode5siffer(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        kvartal,
                        new NæringOgNæringskode5siffer(
                                "10",
                                "10102"
                        )
                ));
        virksomhetMetadataNæringskode5siffer.add(
                new VirksomhetMetadataNæringskode5siffer(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        kvartal,
                        new NæringOgNæringskode5siffer(
                                "20",
                                "20101"
                        )
                ));

        return virksomhetMetadataNæringskode5siffer;
    }

    @NotNull
    private List<Orgenhet> getOrgenhetListe(Kvartal kvartal) {
        List<Orgenhet> orgenhetSomSkalTilVirksomhetMetadata = new ArrayList<>();
        orgenhetSomSkalTilVirksomhetMetadata.add(
                new Orgenhet(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        "Virksomhet 1",
                        "2",
                        "3",
                        "10",
                        kvartal)
        );
        orgenhetSomSkalTilVirksomhetMetadata.add(
                new Orgenhet(
                        new Orgnr(ORGNR_VIRKSOMHET_2),
                        "Virksomhet 2",
                        "2",
                        "3",
                        "20",
                        kvartal)
        );

        return orgenhetSomSkalTilVirksomhetMetadata;
    }

    private List<VirksomhetMetadata> getVirksomhetMetadataListe(Kvartal kvartal) {
        List<VirksomhetMetadata> virksomhetMetadataListe = new ArrayList<>();
        virksomhetMetadataListe.add(
                new VirksomhetMetadata(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        "Virksomhet 1",
                        "2",
                        "3",
                        "10",
                        kvartal
                ));
        virksomhetMetadataListe.add(
                new VirksomhetMetadata(
                        new Orgnr(ORGNR_VIRKSOMHET_2),
                        "Virksomhet 2",
                        "2",
                        "3",
                        "20",
                        kvartal
                ));

        return virksomhetMetadataListe;
    }
}
