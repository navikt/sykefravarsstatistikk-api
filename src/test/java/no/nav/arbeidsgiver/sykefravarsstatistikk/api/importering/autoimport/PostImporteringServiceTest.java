package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.NæringOgNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner.Orgenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.GraderingRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
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
    private ÅrstallOgKvartal __2020_4 = new ÅrstallOgKvartal(2020, 4);

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
                Collections.emptyList(),
                getVirksomhetMetadataListe(__2020_4)
        );

        int antall = service.fullførPostImporteringOgForberedNesteEksport(__2020_4);

        assertEquals(2, antall);
    }

    private void mockForberedNesteEksport(
            ÅrstallOgKvartal årstallOgKvartal,
            List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartalListe,
            List<VirksomhetMetadata> virksomhetMetadataListe
    ) {
        when(
                eksporteringRepository.hentVirksomhetEksportPerKvartal(årstallOgKvartal))
                .thenReturn(virksomhetEksportPerKvartalListe);
        when(
                virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal))
                .thenReturn(virksomhetMetadataListe);
        when(
                eksporteringRepository.opprettEksport(any()))
                .thenReturn(virksomhetMetadataListe.size());
    }

    private void mockImportVirksomhetMetadata(
            ÅrstallOgKvartal årstallOgKvartal,
            List<Orgenhet> orgenhetSomSkalTilVirksomhetMetadata
    ) {
        when(
                datavarehusRepository.hentOrgenhet(årstallOgKvartal, true))
                .thenReturn(orgenhetSomSkalTilVirksomhetMetadata);
        when(
                virksomhetMetadataRepository.opprettVirksomhetMetadata(any()))
                .thenReturn(orgenhetSomSkalTilVirksomhetMetadata.size());
    }

    private void mockImportVirksomhetNæringskode5sifferMapping(
            ÅrstallOgKvartal årstallOgKvartal,
            List<VirksomhetMetadataNæringskode5siffer> virksomhetMetadataNæringskode5sifferListe
    ) {
        when(
                graderingRepository.hentVirksomhetMetadataNæringskode5siffer(årstallOgKvartal))
                .thenReturn(virksomhetMetadataNæringskode5sifferListe);
        when(
                virksomhetMetadataRepository.opprettVirksomhetMetadataNæringskode5siffer(any()))
                .thenReturn(virksomhetMetadataNæringskode5sifferListe.size());
    }

    private List<VirksomhetMetadataNæringskode5siffer> getVirksomhetMetadataNæringskode5sifferListe(
            ÅrstallOgKvartal årstallOgKvartal
    ) {
        List<VirksomhetMetadataNæringskode5siffer> virksomhetMetadataNæringskode5siffer = new ArrayList<>();
        virksomhetMetadataNæringskode5siffer.add(
                new VirksomhetMetadataNæringskode5siffer(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        årstallOgKvartal,
                        new NæringOgNæringskode5siffer(
                                "10",
                                "10101"
                        )
                ));
        virksomhetMetadataNæringskode5siffer.add(
                new VirksomhetMetadataNæringskode5siffer(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        årstallOgKvartal,
                        new NæringOgNæringskode5siffer(
                                "10",
                                "10102"
                        )
                ));
        virksomhetMetadataNæringskode5siffer.add(
                new VirksomhetMetadataNæringskode5siffer(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        årstallOgKvartal,
                        new NæringOgNæringskode5siffer(
                                "20",
                                "20101"
                        )
                ));

        return virksomhetMetadataNæringskode5siffer;
    }

    @NotNull
    private List<Orgenhet> getOrgenhetListe(ÅrstallOgKvartal årstallOgKvartal) {
        List<Orgenhet> orgenhetSomSkalTilVirksomhetMetadata = new ArrayList<>();
        orgenhetSomSkalTilVirksomhetMetadata.add(
                new Orgenhet(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        "Virksomhet 1",
                        "2",
                        "3",
                        "10",
                        årstallOgKvartal)
        );
        orgenhetSomSkalTilVirksomhetMetadata.add(
                new Orgenhet(
                        new Orgnr(ORGNR_VIRKSOMHET_2),
                        "Virksomhet 2",
                        "2",
                        "3",
                        "20",
                        årstallOgKvartal)
        );

        return orgenhetSomSkalTilVirksomhetMetadata;
    }

    private List<VirksomhetMetadata> getVirksomhetMetadataListe(ÅrstallOgKvartal årstallOgKvartal) {
        List<VirksomhetMetadata> virksomhetMetadataListe = new ArrayList<>();
        virksomhetMetadataListe.add(
                new VirksomhetMetadata(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        "Virksomhet 1",
                        "2",
                        "3",
                        "10",
                        årstallOgKvartal
                ));
        virksomhetMetadataListe.add(
                new VirksomhetMetadata(
                        new Orgnr(ORGNR_VIRKSOMHET_2),
                        "Virksomhet 2",
                        "2",
                        "3",
                        "20",
                        årstallOgKvartal
                ));

        return virksomhetMetadataListe;
    }
}