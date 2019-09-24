package no.nav.tag.sykefravarsstatistikk.api.statistikk;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.LandStatistikk;
import no.nav.tag.sykefravarsstatistikk.api.ResourceNotFoundException;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.Sammenligning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

@Component
public class SykefraværprosentService {

    private final SykefraværprosentRepository sykefravarprosentRepository;

    private static int ARSTALL = 2019;
    private static int KVARTAL = 1;

    private static Logger logger = LoggerFactory.getLogger(SykefraværprosentRepository.class);

    public SykefraværprosentService(SykefraværprosentRepository sykefravarprosentRepository) {
        this.sykefravarprosentRepository = sykefravarprosentRepository;
    }

    public Sammenligning hentSammenligning() {
        return new Sammenligning(
                KVARTAL,
                ARSTALL,
                sykefravarprosentRepository.hentSykefraværprosentVirksomhet(ARSTALL, KVARTAL, "910969439"),
                sykefravarprosentRepository.hentSykefraværprosentSektor(ARSTALL, KVARTAL, "0"),
                sykefravarprosentRepository.hentSykefraværprosentNæring(ARSTALL, KVARTAL, "10"),
                sykefravarprosentRepository.hentSykefraværprosentLand(ARSTALL, KVARTAL)
        );
    }

    @Deprecated
    public Sykefraværprosent2 hentSykefraværProsent() {
        LandStatistikk landStatistikk;

        try {
            landStatistikk = sykefravarprosentRepository.hentLandStatistikk(ARSTALL, KVARTAL);
        } catch (EmptyResultDataAccessException erdae) {
            String msg = String.format(
                    "Ingen Landstatistikk funnet for årstall '%d' og kvartal ''%d", ARSTALL, KVARTAL
            );
            logger.info(msg);
            throw new ResourceNotFoundException(msg);
        }
        return Sykefraværprosent2.builder()
                .land(landStatistikk.beregnSykkefravarProsent())
                .build();
    }
}
