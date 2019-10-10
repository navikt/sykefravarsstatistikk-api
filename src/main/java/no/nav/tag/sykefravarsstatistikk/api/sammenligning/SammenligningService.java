package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import org.springframework.stereotype.Component;

@Component
public class SammenligningService {

    private final SammenligningRepository sykefravarprosentRepository;

    private static int ARSTALL = 2019;
    private static int KVARTAL = 1;

    public SammenligningService(SammenligningRepository sykefravarprosentRepository) {
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
}
