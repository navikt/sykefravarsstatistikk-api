package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetEksportPerKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataNæringskode5siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner.Orgenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.GraderingRepository;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Collections;
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
    public int fullførPostImporteringOgForberedNesteEksport(Kvartal kvartal) {
        Pair<Integer, Integer> antallVirksomheterImportert =
                importVirksomhetMetadataOgVirksomhetNæringskode5sifferMapping(kvartal);
        boolean harNoeÅForbereddeTilNesteEksport = antallVirksomheterImportert.getFirst() > 0;

        if (!harNoeÅForbereddeTilNesteEksport) {
            log.info("Post-importering er ferdig. Ingenting å forberedde til neste eksport");
            return 0;
        } else {
            log.info("Post-importering for årstall '{}' og kvartal '{}' er ferdig med " +
                            "'{}' VirksomhetMetadata opprettet og " +
                            "'{}' VirksomhetMetadataNæringskode5siffer opprettet",
                    kvartal.getÅrstall(),
                    kvartal.getKvartal(),
                    antallVirksomheterImportert.getFirst(),
                    antallVirksomheterImportert.getSecond()
            );
        }

        int antallRaderTilNesteEksportering = forberedNesteEksport(kvartal);

        log.info(
                "Forberedelse til neste eksport er ferdig, med '{}' rader klare til neste eksportering " +
                        "(årstall '{}', kvartal '{}')",
                antallRaderTilNesteEksportering,
                kvartal.getÅrstall(),
                kvartal.getKvartal()
        );
        return antallRaderTilNesteEksportering;
    }


    // Kall fra Controller / backdoor
    protected Pair<Integer, Integer> importVirksomhetMetadataOgVirksomhetNæringskode5sifferMapping(
            Kvartal kvartal
    ) {
        if (!erImporteringAktivert) {
            log.info("Importering er ikke aktivert. Skal ikke importere VirksomhetMetadata " +
                    "og VirksomhetNæringskode5sifferMapping");
            return Pair.of(0, 0);
        }

        int antallVirksomhetMetadataOpprettet = importVirksomhetMetadata(kvartal);
        int antallVirksomhetMetadataNæringskode5siffer = importVirksomhetNæringskode5sifferMapping(kvartal);

        log.info(
                "Importering av VirksomhetMetadata og VirksomhetNæringskode5sifferMapping er ferdig. " +
                        "'{}' VirksomhetMetadata og '{}' VirksomhetNæringskode5sifferMapping har blitt importert. ",
                antallVirksomhetMetadataOpprettet,
                antallVirksomhetMetadataNæringskode5siffer
        );
        return Pair.of(antallVirksomhetMetadataOpprettet, antallVirksomhetMetadataNæringskode5siffer);
    }

    // Kall fra Controller / backdoor
    protected int forberedNesteEksport(Kvartal kvartal) {
        if (!erEksporteringAktivert) {
            log.info(
                    "Eksportering er ikke aktivert. " +
                            "Skal ikke forberedde til neste eksportering for årstall '{}' og kvartal '{}'. ",
                    kvartal.getÅrstall(),
                    kvartal.getKvartal()
            );
            return 0;
        }

        int antallIkkeEksportertSykefaværsstatistikk = eksporteringRepository.hentAntallIkkeFerdigEksportert();
        if (antallIkkeEksportertSykefaværsstatistikk > 0) {
            log.warn(
                    "Det finnes '{}' rader  som IKKE er ferdig eksportert. " +
                            "Skal ikke importerer på nytt (slett eksisterende data manuelt før ny import)",
                    antallIkkeEksportertSykefaværsstatistikk
            );
            return 0;
        }
        int antallSlettetEksportertPerKvartal = eksporteringRepository.slettEksportertPerKvartal();
        log.info("Slettet '{}' rader fra forrige eksportering.", antallSlettetEksportertPerKvartal);
        List<VirksomhetMetadata> virksomhetMetadata =
                virksomhetMetadataRepository.hentVirksomhetMetadata(kvartal);

        List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartalListe =
                mapToVirksomhetEksportPerKvartal(virksomhetMetadata);
        log.info(
                "Skal gjøre klar '{}' virksomheter til neste eksportering. ",
                virksomhetEksportPerKvartalListe == null ? 0 : virksomhetEksportPerKvartalListe.size()
        );

        int antallOpprettet = eksporteringRepository.opprettEksport(virksomhetEksportPerKvartalListe);
        log.info("Antall rader opprettet til neste eksportering: {}", antallOpprettet);

        return antallOpprettet;
    }


    private int importVirksomhetMetadata(Kvartal kvartal) {
        List<Orgenhet> orgenhetList = hentOrgenhetListeFraDvh(kvartal);

        if (orgenhetList.isEmpty()) {
            log.warn("Stopper import av metadata.");
            return 0;
        }

        log.info("Antall orgenhet fra DVH: {}", orgenhetList.size());
        int antallSlettet = virksomhetMetadataRepository.slettVirksomhetMetadata();
        log.info("Slettet '{}' VirksomhetMetadata for årstall '{}' og kvartal '{}'",
                antallSlettet,
                kvartal.getÅrstall(),
                kvartal.getKvartal()
        );
        int antallOpprettet =
                virksomhetMetadataRepository.opprettVirksomhetMetadata(mapToVirksomhetMetadata(orgenhetList));
        log.info("Antall rader VirksomhetMetadata opprettet: {}", antallOpprettet);

        return antallOpprettet;
    }

    @Nullable
    private List<Orgenhet> hentOrgenhetListeFraDvh(Kvartal kvartal) {
        List<Orgenhet> orgenhetList = datavarehusRepository.hentOrgenhet(kvartal, true);

        if (orgenhetList.isEmpty()) {
            List<Kvartal> alleSisteTilgjengeligKvartal = datavarehusRepository.hentSisteKvartalForOrgenhet();

            if (alleSisteTilgjengeligKvartal == null || alleSisteTilgjengeligKvartal.isEmpty()) {
                log.warn("Ingen Orgenhet i DVH funnet til import.");
                return Collections.emptyList();
            }

            if (alleSisteTilgjengeligKvartal.size() != 1) {
                log.warn(
                        "Har ikke funnet Orgenhet for årstall '{}' og kvartal '{}'. " +
                                "Flere enn 1 årstal og kvartal funnet i DVH for Orgenhet, antall: '{}'.",
                        kvartal.getÅrstall(),
                        kvartal.getKvartal(),
                        alleSisteTilgjengeligKvartal.size()
                );
                return Collections.emptyList();
            }

            Kvartal tilgjengeligKvartal = alleSisteTilgjengeligKvartal.get(0);
            log.warn(
                    "Har ikke funnet Orgenhet for årstall '{}' og kvartal '{}'. Importerer VirksomhetMetadata " +
                            "med det årstall og kvartal som er tilgjengelig i datavarehus: '{} {}'",
                    kvartal.getÅrstall(),
                    kvartal.getKvartal(),
                    tilgjengeligKvartal.getÅrstall(),
                    tilgjengeligKvartal.getKvartal()
            );
            orgenhetList = datavarehusRepository.hentOrgenhet(kvartal);
        }
        return orgenhetList;
    }

    private int importVirksomhetNæringskode5sifferMapping(Kvartal kvartal) {
        List<VirksomhetMetadataNæringskode5siffer> virksomhetMetadataNæringskode5siffer =
                graderingRepository.hentVirksomhetMetadataNæringskode5siffer(kvartal);

        if (virksomhetMetadataNæringskode5siffer.isEmpty()) {
            log.warn("Ingen virksomhetMetadataNæringskode5siffer funnet i vår statistikk tabell. Stopper import. ");
            return 0;
        }

        int antallSlettetNæringskode5Siffer = virksomhetMetadataRepository.slettNæringOgNæringskode5siffer();
        log.info("Slettet '{}' eksisterende NæringOgNæringskode5siffer. ", antallSlettetNæringskode5Siffer);

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

    private static List<VirksomhetEksportPerKvartal> mapToVirksomhetEksportPerKvartal(
            List<VirksomhetMetadata> virksomhetMetadataList
    ) {
        return virksomhetMetadataList.stream().map(
                virksomhetMetadata -> new VirksomhetEksportPerKvartal(
                        new Orgnr(virksomhetMetadata.getOrgnr()),
                        new Kvartal(
                                virksomhetMetadata.getÅrstall(),
                                virksomhetMetadata.getKvartal()),
                        false
                )
        ).collect(Collectors.toList());
    }
}
