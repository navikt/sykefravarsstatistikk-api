package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner.Orgenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.GraderingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
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
    private final boolean erImporteringAktivert;
    private final boolean erEksporteringAktivert;

    public PostImporteringService(
            DatavarehusRepository datavarehusRepository,
            VirksomhetMetadataRepository virksomhetMetadataRepository,
            GraderingRepository graderingRepository,
            EksporteringRepository eksporteringRepository,
            @Value("${statistikk.importering.aktivert}") Boolean erImporteringAktivert,
            @Value("${statistikk.eksportering.aktivert}") Boolean erEksporteringAktivert) {
        this.datavarehusRepository = datavarehusRepository;
        this.virksomhetMetadataRepository = virksomhetMetadataRepository;
        this.graderingRepository = graderingRepository;
        this.eksporteringRepository = eksporteringRepository;
        this.erImporteringAktivert = erImporteringAktivert;
        this.erEksporteringAktivert = erEksporteringAktivert;
    }

    // Kall fra Scheduler / Importering
    public int fullførPostImporteringOgForberedNesteEksport(ÅrstallOgKvartal årstallOgKvartal) {
        Pair<Integer, Integer> antallVirksomheterImportert =
                importVirksomhetMetadataOgVirksomhetNæringskode5sifferMapping(årstallOgKvartal);

        boolean harNoeÅForbereddeTilNesteEksport = antallVirksomheterImportert.getFirst() > 0;
        if (!harNoeÅForbereddeTilNesteEksport) {
            log.info("Post-importering er ferdig. Ingenting å forberedde til neste eksport");
            return 0;
        } else {
            log.info("Post-importering for årstall '{}' og kvartal '{}' er ferdig med " +
                            "'{}' VirksomhetMetadata opprettet og " +
                            "'{}' VirksomhetMetadataNæringskode5siffer opprettet",
                    årstallOgKvartal.getÅrstall(),
                    årstallOgKvartal.getKvartal(),
                    antallVirksomheterImportert.getFirst(),
                    antallVirksomheterImportert.getSecond()
            );
        }

        int antallRaderTilNesteEksportering = forberedNesteEksport(årstallOgKvartal);

        log.info(
                "Forberedelse til neste eksport er ferdig, med '{}' rader klare til neste eksportering " +
                        "(årstall '{}', kvartal '{}')",
                antallRaderTilNesteEksportering,
                årstallOgKvartal.getÅrstall(),
                årstallOgKvartal.getKvartal()
        );
        return antallRaderTilNesteEksportering;
    }


    // Kall fra Controller / backdoor
    protected Pair<Integer, Integer> importVirksomhetMetadataOgVirksomhetNæringskode5sifferMapping(ÅrstallOgKvartal årstallOgKvartal) {
        if (!erImporteringAktivert) {
            return Pair.of(0, 0);
        }

        int antallVirksomhetMetadataOpprettet = importVirksomhetMetadata(årstallOgKvartal);
        int VirksomhetMetadataNæringskode5siffer = importVirksomhetNæringskode5sifferMapping(årstallOgKvartal);

        return Pair.of(antallVirksomhetMetadataOpprettet, VirksomhetMetadataNæringskode5siffer);
    }

    // Kall fra Controller / backdoor
    protected int forberedNesteEksport(ÅrstallOgKvartal årstallOgKvartal) {
        if (!erEksporteringAktivert) {
            return 0;
        }

        List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartal =
                eksporteringRepository.hentVirksomhetEksportPerKvartal(årstallOgKvartal);

        if (!virksomhetEksportPerKvartal.isEmpty()) {
            log.warn(
                    "Det finnes allerede '{}' rader til eksportering for årstall '{}' og kvartal '{}'. " +
                            "Skal ikke importerer på nytt (slett eksisterende data manuelt før ny import)",
                    virksomhetEksportPerKvartal.size(),
                    årstallOgKvartal.getÅrstall(),
                    årstallOgKvartal.getKvartal()
            );
            return 0;
        }

        List<VirksomhetMetadata> virksomhetMetadata =
                virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal);

        int antallOpprettet = eksporteringRepository.opprettEksport(
                mapToVirksomhetEksportPerKvartal(virksomhetMetadata)
        );

        log.info("Antall rader opprettet til neste eksportering: {}", antallOpprettet);
        return antallOpprettet;
    }


    private int importVirksomhetMetadata(ÅrstallOgKvartal årstallOgKvartal) {
        List<Orgenhet> orgenhetList = datavarehusRepository.hentOrgenhet(årstallOgKvartal);
        int antallOpprettet =
                virksomhetMetadataRepository.opprettVirksomhetMetadata(mapToVirksomhetMetadata(orgenhetList));

        log.info("Antall rader VirksomhetMetadata opprettet: {}", antallOpprettet);
        return antallOpprettet;
    }

    private int importVirksomhetNæringskode5sifferMapping(ÅrstallOgKvartal årstallOgKvartal) {
        List<VirksomhetMetadataNæringskode5siffer> virksomhetMetadataNæringskode5siffer =
                graderingRepository.hentVirksomhetMetadataNæringskode5siffer(årstallOgKvartal);

        int antallOpprettet =
                virksomhetMetadataRepository.opprettVirksomhetMetadataNæringskode5siffer(
                        virksomhetMetadataNæringskode5siffer
                );

        log.info("Antall rader VirksomhetMetadataNæringskode5siffer opprettet: {}", antallOpprettet);
        return antallOpprettet;
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
