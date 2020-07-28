package no.nav.arbeidsgiver.sykefravarsstatistikk.api.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.SlettOgOpprettResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.Statistikkkilde;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.StatistikkkildeDvh;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.DataverehusRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.importering.StatistikkImportRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.importering.StatistikkImportService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ImporteringService {

    private final StatistikkImportRepository statistikkImportRepository;
    private final DataverehusRepository dataverehusRepository;
    private final StatistikkImportService statistikkImportService;

    public ImporteringService(
            StatistikkImportRepository statistikkImportRepository,
            DataverehusRepository dataverehusRepository,
            StatistikkImportService statistikkImportService
    ) {
        this.statistikkImportRepository = statistikkImportRepository;
        this.dataverehusRepository = dataverehusRepository;
        this.statistikkImportService = statistikkImportService;
    }

    public void importerHvisDetFinnesNyStatistikk() {
        List<ÅrstallOgKvartal> årstallOgKvartalForSykefraværsstatistikk = Arrays.asList(
                statistikkImportRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkkilde.LAND),
                statistikkImportRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkkilde.SEKTOR),
                statistikkImportRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkkilde.NÆRING),
                statistikkImportRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkkilde.NÆRING_5_SIFFER),
                statistikkImportRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkkilde.VIRKSOMHET)
        );
        List<ÅrstallOgKvartal> årstallOgKvartalForDvh = Arrays.asList(
                dataverehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkkildeDvh.LAND_OG_SEKTOR),
                dataverehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkkildeDvh.NÆRING),
                dataverehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkkildeDvh.NÆRING_5_SIFFER),
                dataverehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkkildeDvh.VIRKSOMHET)
        );

        if (kanImportStartes(årstallOgKvartalForSykefraværsstatistikk, årstallOgKvartalForDvh)) {
            log.info("Importerer ny statistikk");
            importerNyStatistikk(årstallOgKvartalForDvh.get(0));
        } else {
            log.info("Importerer ikke ny statistikk");
        }
    }

    private void importerNyStatistikk(ÅrstallOgKvartal årstallOgKvartal) {
        SlettOgOpprettResultat resultatLand = statistikkImportService
                .importSykefraværsstatistikkLand(årstallOgKvartal);
        logResultatAvImportering("land", resultatLand, årstallOgKvartal);

        SlettOgOpprettResultat resultatSektor = statistikkImportService
                .importSykefraværsstatistikkSektor(årstallOgKvartal);
        logResultatAvImportering("sektor", resultatSektor, årstallOgKvartal);

        SlettOgOpprettResultat resultatNæring = statistikkImportService
                .importSykefraværsstatistikkNæring(årstallOgKvartal);
        logResultatAvImportering("næring", resultatNæring, årstallOgKvartal);

        SlettOgOpprettResultat resultatNæring5siffer = statistikkImportService
                .importSykefraværsstatistikkNæring(årstallOgKvartal);
        logResultatAvImportering("næring5siffer", resultatNæring5siffer, årstallOgKvartal);

        SlettOgOpprettResultat resultatVirksomhet = statistikkImportService
                .importSykefraværsstatistikkNæring(årstallOgKvartal);
        logResultatAvImportering("virksomhet", resultatVirksomhet, årstallOgKvartal);
    }

    private void logResultatAvImportering(String type, SlettOgOpprettResultat resultat, ÅrstallOgKvartal årstallOgKvartal) {
        log.info("Importert ny statistikk for {} fra årstall {} og kvartal {}. Antall rader slettet: {}, antall rader opprettet: {}",
                type,
                årstallOgKvartal.getÅrstall(),
                årstallOgKvartal.getKvartal(),
                resultat.getAntallRadSlettet(),
                resultat.getAntallRadOpprettet()
        );
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

    private boolean alleErLike(List<ÅrstallOgKvartal> årstallOgKvartal) {
        ÅrstallOgKvartal førsteÅrstallOgKvartal = årstallOgKvartal.get(0);
        return årstallOgKvartal.stream().allMatch(p -> p.equals(førsteÅrstallOgKvartal));
    }
}
