package no.nav.arbeidsgiver.sykefravarsstatistikk.api.autoimport;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.Statistikkkilde;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.StatistikkkildeDvh;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.DataverehusRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.importering.StatistikkImportRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ImporteringService {

    final StatistikkImportRepository statistikkImportRepository;
    final DataverehusRepository dataverehusRepository;

    public ImporteringService(StatistikkImportRepository statistikkImportRepository, DataverehusRepository dataverehusRepository) {
        this.statistikkImportRepository = statistikkImportRepository;
        this.dataverehusRepository = dataverehusRepository;
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
            //TODO
        } else {
            log.info("Fant ingen ny statistikk");
        }
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
