package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.SlettOgOpprettResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.Importeringsobjekt;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.StatistikkRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner.Orgenhet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ImporteringService {

    private final StatistikkRepository statistikkRepository;
    private final DatavarehusRepository datavarehusRepository;
    private final VirksomhetMetadataRepository virksomhetMetadataRepository;
    private final boolean erImporteringAktivert;

    public ImporteringService(
            StatistikkRepository statistikkRepository,
            DatavarehusRepository datavarehusRepository,
            VirksomhetMetadataRepository virksomhetMetadataRepository,
            @Value("${statistikk.importering.aktivert}") Boolean erImporteringAktivert) {
        this.statistikkRepository = statistikkRepository;
        this.datavarehusRepository = datavarehusRepository;
        this.virksomhetMetadataRepository = virksomhetMetadataRepository;
        this.erImporteringAktivert = erImporteringAktivert;
    }

    public void importerHvisDetFinnesNyStatistikk() {
        log.info("Er importering aktivert? {}", erImporteringAktivert);

        List<ÅrstallOgKvartal> årstallOgKvartalForSykefraværsstatistikk = Arrays.asList(
                statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.LAND),
                statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.SEKTOR),
                statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.NÆRING),
                statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.NÆRING_5_SIFFER),
                statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.VIRKSOMHET)
        );
        List<ÅrstallOgKvartal> årstallOgKvartalForDvh = Arrays.asList(
                datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.LAND_OG_SEKTOR),
                datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.NÆRING),
                datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.NÆRING_5_SIFFER),
                datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.VIRKSOMHET)
        );

        if (kanImportStartes(årstallOgKvartalForSykefraværsstatistikk, årstallOgKvartalForDvh)) {
            if (erImporteringAktivert) {
                log.info("Importerer ny statistikk");
                importerNyStatistikk(årstallOgKvartalForDvh.get(0));
                int antallVirksomhetMetadataOpprettet = importVirksomhetMetadata(årstallOgKvartalForDvh.get(0));
                log.info("Har lagret {} virksomheter klare til eksport", antallVirksomhetMetadataOpprettet);
            } else {
                log.info("Statistikk er klar til importering men automatisk importering er ikke aktivert");
            }
        } else {
            log.info("Importerer ikke ny statistikk");
        }
    }

    public void reimporterSykefraværsstatistikk(ÅrstallOgKvartal fra, ÅrstallOgKvartal til) {
        ÅrstallOgKvartal.range(fra, til).forEach(this::importerNyStatistikk);
    }

    public void reimporterSykefraværsstatistikk(ÅrstallOgKvartal fra, ÅrstallOgKvartal til, List<Importeringsobjekt> importeringsobjekter) {
        ÅrstallOgKvartal.range(fra, til).forEach((årstallOgKvartal) -> importerNyStatistikk(årstallOgKvartal, importeringsobjekter));
    }


    protected boolean kanImportStartes(
            List<ÅrstallOgKvartal> årstallOgKvartalForSykefraværsstatistikk,
            List<ÅrstallOgKvartal> årstallOgKvartalForDvh
    ) {

        boolean allImportertStatistikkHarSammeÅrstallOgKvartal = alleErLike(årstallOgKvartalForSykefraværsstatistikk);
        boolean allStatistikkFraDvhHarSammeÅrstallOgKvartal = alleErLike(årstallOgKvartalForDvh);

        if (!allImportertStatistikkHarSammeÅrstallOgKvartal || !allStatistikkFraDvhHarSammeÅrstallOgKvartal) {
            log.warn("Kunne ikke importere ny statistikk, statistikk hadde forskjellige årstall og kvartal. " +
                            "Har importert statistikk samme årstall og kvartal? {}. " +
                            "Har statistikk fra Dvh samme årstall og kvartal? {}",
                    allImportertStatistikkHarSammeÅrstallOgKvartal,
                    allStatistikkFraDvhHarSammeÅrstallOgKvartal
            );
            return false;
        }

        ÅrstallOgKvartal sisteÅrstallOgKvartalForDvh = årstallOgKvartalForDvh.get(0);
        ÅrstallOgKvartal sisteÅrstallOgKvartalForSykefraværsstatistikk = årstallOgKvartalForSykefraværsstatistikk.get(0);

        boolean importertStatistikkLiggerEttKvartalBakDvh =
                sisteÅrstallOgKvartalForDvh.minusKvartaler(1).equals(sisteÅrstallOgKvartalForSykefraværsstatistikk);

        if (importertStatistikkLiggerEttKvartalBakDvh) {
            log.info("Skal importere statistikk fra Dvh for årstall {} og kvartal {}",
                    sisteÅrstallOgKvartalForDvh.getÅrstall(),
                    sisteÅrstallOgKvartalForDvh.getKvartal()
            );
            return true;
        } else if (sisteÅrstallOgKvartalForDvh.equals(sisteÅrstallOgKvartalForSykefraværsstatistikk)) {
            log.info("Skal ikke importere statistikk fra Dvh for årstall {} og kvartal {}. Ingen ny statistikk funnet.",
                    sisteÅrstallOgKvartalForDvh.getÅrstall(),
                    sisteÅrstallOgKvartalForDvh.getKvartal()
            );
            return false;
        } else {
            log.warn("Kunne ikke importere ny statistikk fra Dvh fordi årstall {} og kvartal {} ikke ligger nøyaktig " +
                            "ett kvartal foran vår statistikk som har årstall {} og kvartal {}.",
                    sisteÅrstallOgKvartalForDvh.getÅrstall(),
                    sisteÅrstallOgKvartalForDvh.getKvartal(),
                    sisteÅrstallOgKvartalForSykefraværsstatistikk.getÅrstall(),
                    sisteÅrstallOgKvartalForSykefraværsstatistikk.getKvartal()
            );
            return false;
        }
    }

    private void importerNyStatistikk(ÅrstallOgKvartal årstallOgKvartal, List<Importeringsobjekt> importeringsobjekter) {
        if (importeringsobjekter.contains(Importeringsobjekt.LAND)) {
            importSykefraværsstatistikkLand(årstallOgKvartal);
        }

        if (importeringsobjekter.contains(Importeringsobjekt.SEKTOR)) {
            importSykefraværsstatistikkSektor(årstallOgKvartal);
        }

        if (importeringsobjekter.contains(Importeringsobjekt.NÆRING)) {
            importSykefraværsstatistikkNæring(årstallOgKvartal);
        }

        if (importeringsobjekter.contains(Importeringsobjekt.NÆRING_5_SIFFER)) {
            importSykefraværsstatistikkNæring5siffer(årstallOgKvartal);
        }

        if (importeringsobjekter.contains(Importeringsobjekt.VIRKSOMHET)) {
            importSykefraværsstatistikkVirksomhet(årstallOgKvartal);
        }

        if (importeringsobjekter.contains(Importeringsobjekt.NÆRING_MED_VARIGHET)) {
            importSykefraværsstatistikkNæringMedVarighet(årstallOgKvartal);
        }

        if (importeringsobjekter.contains(Importeringsobjekt.GRADERING)) {
            importSykefraværsstatistikkMedGradering(årstallOgKvartal);
        }
    }

    private void importerNyStatistikk(ÅrstallOgKvartal årstallOgKvartal) {
        importSykefraværsstatistikkLand(årstallOgKvartal);
        importSykefraværsstatistikkSektor(årstallOgKvartal);
        importSykefraværsstatistikkNæring(årstallOgKvartal);
        importSykefraværsstatistikkNæring5siffer(årstallOgKvartal);
        importSykefraværsstatistikkVirksomhet(årstallOgKvartal);
        importSykefraværsstatistikkNæringMedVarighet(årstallOgKvartal);
        importSykefraværsstatistikkMedGradering(årstallOgKvartal);
    }

    // #1: Hente ut alle virksomheter fra dt_p.v_dim_ia_orgenhet (i DATAVAREHUS)
    // #2: Hente ut alle næring/næringskode5siffer fra sykefravar_statistikk_virksomhet_med_gradering
    protected int importVirksomhetMetadata(ÅrstallOgKvartal årstallOgKvartal) {
        return 0;
    }

    // #3: Fra virksomhet_metadata opprett tilsvarende rader i eksport_per_kvartal for en vis ÅrstallOgKvartal
    protected int forberedNesteEksport(ÅrstallOgKvartal årstallOgKvartal) {
        List<VirksomhetMetadata> eksisterendeVirksomhetMetadataList =
                virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal);

        if (eksisterendeVirksomhetMetadataList.size() == 0) {
            List<Orgenhet> orgenhetList =
                    datavarehusRepository.hentOrgenhet(årstallOgKvartal);
            return virksomhetMetadataRepository.opprettVirksomhetMetadata(mapToVirksomhetMetadata(orgenhetList));
        } else {
            log.info(
                    "Det fins allerede virksomhet metdata for gjelende kvartal {}. Skal ikke legge til nye.",
                    årstallOgKvartal.toString()
            );
            return 0;
        }
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

    private SlettOgOpprettResultat importSykefraværsstatistikkLand(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkLand> sykefraværsstatistikkLand =
                datavarehusRepository.hentSykefraværsstatistikkLand(årstallOgKvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkLand(
                sykefraværsstatistikkLand,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "land");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkSektor(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
                datavarehusRepository.hentSykefraværsstatistikkSektor(årstallOgKvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkSektor(
                sykefraværsstatistikkSektor,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "sektor");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkNæring(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
                datavarehusRepository.hentSykefraværsstatistikkNæring(årstallOgKvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkNæring(
                sykefraværsstatistikkNæring,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "næring");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkNæring5siffer(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
                datavarehusRepository.hentSykefraværsstatistikkNæring5siffer(årstallOgKvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkNæring5siffer(
                sykefraværsstatistikkNæring,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "næring5siffer");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkVirksomhet(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhet =
                datavarehusRepository.hentSykefraværsstatistikkVirksomhet(årstallOgKvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkVirksomhet(
                sykefraværsstatistikkVirksomhet,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "virksomhet");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkMedGradering(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkVirksomhetMedGradering> sykefraværsstatistikkVirksomhetMedGradering =
                datavarehusRepository.hentSykefraværsstatistikkVirksomhetMedGradering(årstallOgKvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkVirksomhetMedGradering(
                sykefraværsstatistikkVirksomhetMedGradering,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "virksomhet gradert sykemelding");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkNæringMedVarighet(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkNæringMedVarighet> sykefraværsstatistikkNæringMedVarighet =
                datavarehusRepository.hentSykefraværsstatistikkNæringMedVarighet(årstallOgKvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkNæringMedVarighet(
                sykefraværsstatistikkNæringMedVarighet,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "næring med varighet");

        return resultat;
    }

    private static void loggResultat(ÅrstallOgKvartal årstallOgKvartal, SlettOgOpprettResultat resultat, String type) {
        String melding = resultat.getAntallRadOpprettet() == 0 && resultat.getAntallRadSlettet() == 0 ?
                "Ingenting å importere"
                :
                String.format(
                        "Antall opprettet: %d, antall slettet: %d",
                        resultat.getAntallRadOpprettet(),
                        resultat.getAntallRadSlettet()
                );

        log.info(
                String.format(
                        "Import av sykefraværsstatistikk (%s) for årstall '%d' og kvartal '%d 'er ferdig. %s",
                        type,
                        årstallOgKvartal.getÅrstall(),
                        årstallOgKvartal.getKvartal(),
                        melding
                )
        );
    }

    private boolean alleErLike(List<ÅrstallOgKvartal> årstallOgKvartal) {
        ÅrstallOgKvartal førsteÅrstallOgKvartal = årstallOgKvartal.get(0);
        return årstallOgKvartal.stream().allMatch(p -> p.equals(førsteÅrstallOgKvartal));
    }
}
