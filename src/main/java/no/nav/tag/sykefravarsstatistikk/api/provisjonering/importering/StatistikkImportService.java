package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.common.SlettOgOpprettResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.*;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.DataverehusRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile({"local", "dev"})
@Slf4j
@Component
public class StatistikkImportService {

    private final DataverehusRepository datavarehusRepository;
    private final StatistikkImportRepository statistikkImportRepository;

    public StatistikkImportService(
            DataverehusRepository datavarehusRepository,
            StatistikkImportRepository statistikkImportRepository) {
        this.datavarehusRepository = datavarehusRepository;
        this.statistikkImportRepository = statistikkImportRepository;
    }

    // TODO: DELETE ME --> bare til versifisering
    public List<SykefraværsstatistikkLand> hentSykefraværsstatistikkLand(ÅrstallOgKvartal årstallOgKvartal) {
        return datavarehusRepository.hentSykefraværsstatistikkLand(årstallOgKvartal);
    }

    public List<SykefraværsstatistikkSektor> hentSykefraværsstatistikkSektor(ÅrstallOgKvartal årstallOgKvartal) {
        return datavarehusRepository.hentSykefraværsstatistikkSektor(årstallOgKvartal);
    }

    public List<SykefraværsstatistikkNæring> hentSykefraværsstatistikkNæring(ÅrstallOgKvartal årstallOgKvartal) {
        return datavarehusRepository.hentSykefraværsstatistikkNæring(årstallOgKvartal);
    }

    public List<SykefraværsstatistikkVirksomhet> hentSykefraværsstatistikkVirksomhet(ÅrstallOgKvartal årstallOgKvartal) {
        return datavarehusRepository.hentSykefraværsstatistikkVirksomhet(årstallOgKvartal);
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkLand(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkLand> sykefraværsstatistikkLand =
                datavarehusRepository.hentSykefraværsstatistikkLand(årstallOgKvartal);

        SlettOgOpprettResultat resultat = statistikkImportRepository.importSykefraværsstatistikkLand(
                sykefraværsstatistikkLand,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "land");

        return resultat;
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkSektor(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
                datavarehusRepository.hentSykefraværsstatistikkSektor(årstallOgKvartal);

        SlettOgOpprettResultat resultat = statistikkImportRepository.importSykefraværsstatistikkSektor(
                sykefraværsstatistikkSektor,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "sektor");

        return resultat;
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkNæring(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkNæring> sykefraværsstatistikkNæring =
                datavarehusRepository.hentSykefraværsstatistikkNæring(årstallOgKvartal);

        SlettOgOpprettResultat resultat = statistikkImportRepository.importSykefraværsstatistikkNæring(
                sykefraværsstatistikkNæring,
                årstallOgKvartal
        );
        loggResultat(årstallOgKvartal, resultat, "næring");

        return resultat;
    }

    public SlettOgOpprettResultat importSykefraværsstatistikkVirksomhet(ÅrstallOgKvartal årstallOgKvartal) {
        List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhet =
                datavarehusRepository.hentSykefraværsstatistikkVirksomhet(årstallOgKvartal);

        SlettOgOpprettResultat resultat = statistikkImportRepository.importSykefraværsstatistikkVirksomhet(
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

}
