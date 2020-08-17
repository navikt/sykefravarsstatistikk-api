package no.nav.arbeidsgiver.sykefravarsstatistikk.api.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.SlettOgOpprettResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.DatavarehusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ImporteringService {

    private final ImporteringRepository importeringRepository;
    private final DatavarehusRepository datavarehusRepository;
    private final boolean erImporteringAktivert;

    public ImporteringService(
            ImporteringRepository importeringRepository,
            DatavarehusRepository datavarehusRepository,
            @Value("${statistikk.importering.aktivert}")
            Boolean erImporteringAktivert) {
        this.importeringRepository = importeringRepository;
        this.datavarehusRepository = datavarehusRepository;
        this.erImporteringAktivert = erImporteringAktivert;
    }

    public void importerHvisDetFinnesNyStatistikk() {
        log.info("Er importering aktivert? {}", erImporteringAktivert);

        List<ÅrstallOgKvartal> årstallOgKvartalForSykefraværsstatistikk = Arrays.asList(
                importeringRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.LAND),
                importeringRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.SEKTOR),
                importeringRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.NÆRING),
                importeringRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.NÆRING_5_SIFFER),
                importeringRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.VIRKSOMHET)
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

        if (importertStatistikkLiggerEttKvartalBakDvh){
            log.info("Skal importere statistikk fra Dvh for årstall {} og kvartal {}",
                    sisteÅrstallOgKvartalForDvh.getÅrstall(),
                    sisteÅrstallOgKvartalForDvh.getKvartal()
            );
            return true;
        } else if (sisteÅrstallOgKvartalForDvh.equals(sisteÅrstallOgKvartalForSykefraværsstatistikk)){
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

    private void importerNyStatistikk(ÅrstallOgKvartal årstallOgKvartal) {
        importSykefraværsstatistikkLand(årstallOgKvartal);
        importSykefraværsstatistikkSektor(årstallOgKvartal);
        importSykefraværsstatistikkNæring(årstallOgKvartal);
        importSykefraværsstatistikkNæring5siffer(årstallOgKvartal);
        importSykefraværsstatistikkVirksomhet(årstallOgKvartal);
    }


    private SlettOgOpprettResultat importSykefraværsstatistikkLand(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkLand> sykefraværsstatistikkLand =
                datavarehusRepository.hentSykefraværsstatistikkLand(årstallOgKvartal);

        SlettOgOpprettResultat resultat = importeringRepository.importSykefraværsstatistikkLand(
                sykefraværsstatistikkLand,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "land");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkSektor(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
                datavarehusRepository.hentSykefraværsstatistikkSektor(årstallOgKvartal);

        SlettOgOpprettResultat resultat = importeringRepository.importSykefraværsstatistikkSektor(
                sykefraværsstatistikkSektor,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "sektor");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkNæring(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
                datavarehusRepository.hentSykefraværsstatistikkNæring(årstallOgKvartal);

        SlettOgOpprettResultat resultat = importeringRepository.importSykefraværsstatistikkNæring(
                sykefraværsstatistikkNæring,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "næring");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkNæring5siffer(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
                datavarehusRepository.hentSykefraværsstatistikkNæring5siffer(årstallOgKvartal);

        SlettOgOpprettResultat resultat = importeringRepository.importSykefraværsstatistikkNæring5siffer(
                sykefraværsstatistikkNæring,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "næring5siffer");

        return resultat;
    }

    private SlettOgOpprettResultat importSykefraværsstatistikkVirksomhet(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhet =
                datavarehusRepository.hentSykefraværsstatistikkVirksomhet(årstallOgKvartal);

        SlettOgOpprettResultat resultat = importeringRepository.importSykefraværsstatistikkVirksomhet(
                sykefraværsstatistikkVirksomhet,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "virksomhet");

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
