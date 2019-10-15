package no.nav.tag.sykefravarsstatistikk.api.provisionering;

import no.nav.tag.sykefravarsstatistikk.api.domene.klassifikasjoner.Sektor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProvisioneringService {

    private final DataverehusRepository datavarehusRepository;
    private final ProvisioneringRepository provisioneringRepository;

    public ProvisioneringService(
            DataverehusRepository datavarehusRepository,
            ProvisioneringRepository provisioneringRepository
    ) {
        this.datavarehusRepository = datavarehusRepository;
        this.provisioneringRepository = provisioneringRepository;
    }

    public void populerSektorer(int årstall, int kvartal) {
        List<Sektor> sektorer = datavarehusRepository.hentAlleSektorer(årstall, kvartal);
        provisioneringRepository.oppdaterSektorer(sektorer);
    }
}
