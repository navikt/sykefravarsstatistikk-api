package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksportRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.SlettOgOpprettResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.Importeringsobjekt;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.StatistikkRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner.Orgenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.GraderingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PostImporteringService {

    private final DatavarehusRepository datavarehusRepository;
    private final VirksomhetMetadataRepository virksomhetMetadataRepository;
    private final GraderingRepository graderingRepository;
    private final EksportRepository eksportRepository;
    private final boolean erImporteringAktivert;

    public PostImporteringService(
            DatavarehusRepository datavarehusRepository,
            VirksomhetMetadataRepository virksomhetMetadataRepository,
            GraderingRepository graderingRepository,
            EksportRepository eksportRepository,
            @Value("${statistikk.importering.aktivert}") Boolean erImporteringAktivert) {
        this.datavarehusRepository = datavarehusRepository;
        this.virksomhetMetadataRepository = virksomhetMetadataRepository;
        this.graderingRepository = graderingRepository;
        this.eksportRepository = eksportRepository;
        this.erImporteringAktivert = erImporteringAktivert;
    }

    public int importVirksomhetMetadata(ÅrstallOgKvartal årstallOgKvartal) {
        List<Orgenhet> orgenhetList = datavarehusRepository.hentOrgenhet(årstallOgKvartal);

        return virksomhetMetadataRepository.opprettVirksomhetMetadata(mapToVirksomhetMetadata(orgenhetList));
    }

    public int importVirksomhetNæringskode5sifferMapping(ÅrstallOgKvartal årstallOgKvartal) {
        List<VirksomhetMetadataNæringskode5siffer> virksomhetMetadataNæringskode5siffer =
                graderingRepository.hentVirksomhetMetadataNæringskode5siffer(årstallOgKvartal);

        return virksomhetMetadataRepository.opprettVirksomhetMetadataNæringskode5siffer(virksomhetMetadataNæringskode5siffer);
    }

    public int forberedNesteEksport(ÅrstallOgKvartal årstallOgKvartal) {
        // TODO: sjekk at eksport er ferdig for årstallOgKvartal ---> ingen rad for årstallOgKvartal
        List<VirksomhetMetadata> virksomhetMetadata =
                virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal);

        return eksportRepository.opprettEksport(virksomhetMetadata);
    }


    private static List<VirksomhetMetadata> mapToVirksomhetMetadata(List<Orgenhet> orgenhetList) {
        return orgenhetList.stream().map(
                orgenhet -> new VirksomhetMetadata(
                        orgenhet.getOrgnr(),
                        orgenhet.getNavn(),
                        orgenhet.getRectype(),
                        orgenhet.getSektor(),
                        orgenhet.getNæring(),
                        orgenhet.getÅrstallOgKvartal()
                )
        ).collect(Collectors.toList());
    }
}
