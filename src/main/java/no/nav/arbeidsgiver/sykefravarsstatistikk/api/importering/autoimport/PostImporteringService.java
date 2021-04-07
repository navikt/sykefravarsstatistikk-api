package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner.Orgenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.GraderingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PostImporteringService {

    private final DatavarehusRepository datavarehusRepository;
    private final VirksomhetMetadataRepository virksomhetMetadataRepository;
    private final GraderingRepository graderingRepository;
    private final EksporteringRepository eksporteringRepository;
    private final boolean erEksporteringAktivert;

    public PostImporteringService(
            DatavarehusRepository datavarehusRepository,
            VirksomhetMetadataRepository virksomhetMetadataRepository,
            GraderingRepository graderingRepository,
            EksporteringRepository eksporteringRepository,
            @Value("${statistikk.eksportering.aktivert}") Boolean erEksporteringAktivert) {
        this.datavarehusRepository = datavarehusRepository;
        this.virksomhetMetadataRepository = virksomhetMetadataRepository;
        this.graderingRepository = graderingRepository;
        this.eksporteringRepository = eksporteringRepository;
        this.erEksporteringAktivert = erEksporteringAktivert;
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
        if (!erEksporteringAktivert) {
            return 0;
        }
        List<VirksomhetMetadata> virksomhetMetadata =
                virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal);

        return eksporteringRepository.opprettEksport(
                mapToVirksomhetEksportPerKvartal(virksomhetMetadata)
        );
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

    private static List<VirksomhetEksportPerKvartal> mapToVirksomhetEksportPerKvartal(List<VirksomhetMetadata> virksomhetMetadataList) {
        return virksomhetMetadataList.stream().map(
                virksomhetMetadata -> new VirksomhetEksportPerKvartal(
                        new Orgnr(virksomhetMetadata.getOrgnr()),
                        new ÅrstallOgKvartal(
                                virksomhetMetadata.getÅrstall(),
                                virksomhetMetadata.getKvartal()),
                        false
                )
        ).collect(Collectors.toList());
    }
}
