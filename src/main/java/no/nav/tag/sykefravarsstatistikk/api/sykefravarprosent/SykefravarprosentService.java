package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosent;

import no.nav.tag.sykefravarsstatistikk.api.domain.stats.LandStatistikk;
import no.nav.tag.sykefravarsstatistikk.api.repository.ResourceNotFoundException;
import no.nav.tag.sykefravarsstatistikk.api.repository.SykefravarprosentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

@Component
public class SykefravarprosentService {

    private final SykefravarprosentRepository sykefravarprosentRepository;

    private static int ARSTALL = 2019;
    private static int KVARTAL = 1;

    private static Logger logger = LoggerFactory.getLogger(SykefravarprosentRepository.class);

    public SykefravarprosentService(SykefravarprosentRepository sykefravarprosentRepository) {
        this.sykefravarprosentRepository = sykefravarprosentRepository;
    }

    public Sykefravarprosent hentSykefravarProsent() {
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
        return new Sykefravarprosent(landStatistikk);
    }
}
