package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.SlettOgOpprettResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Statistikkilde;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.StatistikkildeDvh;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkLand;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæringMedVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhetMedGradering;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.Importeringsobjekt;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.StatistikkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ImporteringService {

    private final StatistikkRepository statistikkRepository;
    private final DatavarehusRepository datavarehusRepository;
    private final boolean erImporteringAktivert;

    public ImporteringService(
            StatistikkRepository statistikkRepository,
            DatavarehusRepository datavarehusRepository,
            @Value("${statistikk.importering.aktivert}") Boolean erImporteringAktivert) {
        this.statistikkRepository = statistikkRepository;
        this.datavarehusRepository = datavarehusRepository;
        this.erImporteringAktivert = erImporteringAktivert;
    }

    public void importerHvisDetFinnesNyStatistikk() {
        log.info("Er importering aktivert? {}", erImporteringAktivert);

        List<Kvartal> kvartalForSykefraværsstatistikk = Arrays.asList(
                statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.LAND),
                statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.SEKTOR),
                statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.NÆRING),
                statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.NÆRING_5_SIFFER),
                statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.VIRKSOMHET)
        );
        List<Kvartal> kvartalForDvh = Arrays.asList(
                datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.LAND_OG_SEKTOR),
                datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.NÆRING),
                datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.NÆRING_5_SIFFER),
                datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.VIRKSOMHET)
        );

        if (kanImportStartes(kvartalForSykefraværsstatistikk, kvartalForDvh)) {
            if (erImporteringAktivert) {
                log.info("Importerer ny statistikk");
                importerNyStatistikk(kvartalForDvh.get(0));
                //TODO: kall postImport
            } else {
                log.info("Statistikk er klar til importering men automatisk importering er ikke aktivert");
            }
        } else {
            log.info("Importerer ikke ny statistikk");
        }
    }

    public void reimporterSykefraværsstatistikk(Kvartal fra, Kvartal til) {
        Kvartal.range(fra, til).forEach(this::importerNyStatistikk);
    }

    public void reimporterSykefraværsstatistikk(Kvartal fra, Kvartal til, List<Importeringsobjekt> importeringsobjekter) {
        Kvartal.range(fra, til).forEach((årstallOgKvartal) -> importerNyStatistikk(årstallOgKvartal, importeringsobjekter));
    }


    protected boolean kanImportStartes(
            List<Kvartal> kvartalForSykefraværsstatistikk,
            List<Kvartal> kvartalForDvh
    ) {

        boolean allImportertStatistikkHarSammeÅrstallOgKvartal = alleErLike(kvartalForSykefraværsstatistikk);
        boolean allStatistikkFraDvhHarSammeÅrstallOgKvartal = alleErLike(kvartalForDvh);

        if (!allImportertStatistikkHarSammeÅrstallOgKvartal || !allStatistikkFraDvhHarSammeÅrstallOgKvartal) {
            log.warn("Kunne ikke importere ny statistikk, statistikk hadde forskjellige årstall og kvartal. " +
                            "Har importert statistikk samme årstall og kvartal? {}. " +
                            "Har statistikk fra Dvh samme årstall og kvartal? {}",
                    allImportertStatistikkHarSammeÅrstallOgKvartal,
                    allStatistikkFraDvhHarSammeÅrstallOgKvartal
            );
            return false;
        }

        Kvartal sisteKvartalForDvh = kvartalForDvh.get(0);
        Kvartal sisteKvartalForSykefraværsstatistikk = kvartalForSykefraværsstatistikk.get(0);

        boolean importertStatistikkLiggerEttKvartalBakDvh =
                sisteKvartalForDvh.minusKvartaler(1).equals(sisteKvartalForSykefraværsstatistikk);

        if (importertStatistikkLiggerEttKvartalBakDvh) {
            log.info("Skal importere statistikk fra Dvh for årstall {} og kvartal {}",
                    sisteKvartalForDvh.getÅrstall(),
                    sisteKvartalForDvh.getKvartal()
            );
            return true;
        } else if (sisteKvartalForDvh.equals(sisteKvartalForSykefraværsstatistikk)) {
            log.info("Skal ikke importere statistikk fra Dvh for årstall {} og kvartal {}. Ingen ny statistikk funnet.",
                    sisteKvartalForDvh.getÅrstall(),
                    sisteKvartalForDvh.getKvartal()
            );
            return false;
        } else {
            log.warn("Kunne ikke importere ny statistikk fra Dvh fordi årstall {} og kvartal {} ikke ligger nøyaktig " +
                            "ett kvartal foran vår statistikk som har årstall {} og kvartal {}.",
                    sisteKvartalForDvh.getÅrstall(),
                    sisteKvartalForDvh.getKvartal(),
                    sisteKvartalForSykefraværsstatistikk.getÅrstall(),
                    sisteKvartalForSykefraværsstatistikk.getKvartal()
            );
            return false;
        }
    }

    private void importerNyStatistikk(Kvartal kvartal, List<Importeringsobjekt> importeringsobjekter) {
        if (importeringsobjekter.contains(Importeringsobjekt.LAND)) {
            importSykefraværsstatistikkLand(kvartal);
        }

        if (importeringsobjekter.contains(Importeringsobjekt.SEKTOR)) {
            importSykefraværsstatistikkSektor(kvartal);
        }

        if (importeringsobjekter.contains(Importeringsobjekt.NÆRING)) {
            importSykefraværsstatistikkNæring(kvartal);
        }

        if (importeringsobjekter.contains(Importeringsobjekt.NÆRING_5_SIFFER)) {
            importSykefraværsstatistikkNæring5siffer(kvartal);
        }

        if (importeringsobjekter.contains(Importeringsobjekt.VIRKSOMHET)) {
            importSykefraværsstatistikkVirksomhet(kvartal);
        }

        if (importeringsobjekter.contains(Importeringsobjekt.NÆRING_MED_VARIGHET)) {
            importSykefraværsstatistikkNæringMedVarighet(kvartal);
        }

        if (importeringsobjekter.contains(Importeringsobjekt.GRADERING)) {
            importSykefraværsstatistikkMedGradering(kvartal);
        }
    }

    private void importerNyStatistikk(Kvartal kvartal) {
        importSykefraværsstatistikkLand(kvartal);
        importSykefraværsstatistikkSektor(kvartal);
        importSykefraværsstatistikkNæring(kvartal);
        importSykefraværsstatistikkNæring5siffer(kvartal);
        importSykefraværsstatistikkVirksomhet(kvartal);
        importSykefraværsstatistikkNæringMedVarighet(kvartal);
        importSykefraværsstatistikkMedGradering(kvartal);
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkLand(Kvartal kvartal) {
        List<SykefraværsstatistikkLand> sykefraværsstatistikkLand =
                datavarehusRepository.hentSykefraværsstatistikkLand(kvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkLand(
                sykefraværsstatistikkLand,
                kvartal
        );
        loggResultat(kvartal, resultat, "land");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkSektor(Kvartal kvartal) {
        List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
                datavarehusRepository.hentSykefraværsstatistikkSektor(kvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkSektor(
                sykefraværsstatistikkSektor,
                kvartal
        );
        loggResultat(kvartal, resultat, "sektor");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkNæring(Kvartal kvartal) {
        List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
                datavarehusRepository.hentSykefraværsstatistikkNæring(kvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkNæring(
                sykefraværsstatistikkNæring,
                kvartal
        );
        loggResultat(kvartal, resultat, "næring");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkNæring5siffer(Kvartal kvartal) {
        List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
                datavarehusRepository.hentSykefraværsstatistikkNæring5siffer(kvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkNæring5siffer(
                sykefraværsstatistikkNæring,
                kvartal
        );
        loggResultat(kvartal, resultat, "næring5siffer");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkVirksomhet(Kvartal kvartal) {
        List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhet =
                datavarehusRepository.hentSykefraværsstatistikkVirksomhet(kvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkVirksomhet(
                sykefraværsstatistikkVirksomhet,
                kvartal
        );
        loggResultat(kvartal, resultat, "virksomhet");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkMedGradering(Kvartal kvartal) {
        List<SykefraværsstatistikkVirksomhetMedGradering> sykefraværsstatistikkVirksomhetMedGradering =
                datavarehusRepository.hentSykefraværsstatistikkVirksomhetMedGradering(kvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkVirksomhetMedGradering(
                sykefraværsstatistikkVirksomhetMedGradering,
                kvartal
        );
        loggResultat(kvartal, resultat, "virksomhet gradert sykemelding");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkNæringMedVarighet(Kvartal kvartal) {
        List<SykefraværsstatistikkNæringMedVarighet> sykefraværsstatistikkNæringMedVarighet =
                datavarehusRepository.hentSykefraværsstatistikkNæringMedVarighet(kvartal);

        SlettOgOpprettResultat resultat = statistikkRepository.importSykefraværsstatistikkNæringMedVarighet(
                sykefraværsstatistikkNæringMedVarighet,
                kvartal
        );
        loggResultat(kvartal, resultat, "næring med varighet");

        return resultat;
    }

    private static void loggResultat(Kvartal kvartal, SlettOgOpprettResultat resultat, String type) {
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
                        kvartal.getÅrstall(),
                        kvartal.getKvartal(),
                        melding
                )
        );
    }

    private boolean alleErLike(List<Kvartal> kvartal) {
        Kvartal førsteKvartal = kvartal.get(0);
        return kvartal.stream().allMatch(p -> p.equals(førsteKvartal));
    }
}
