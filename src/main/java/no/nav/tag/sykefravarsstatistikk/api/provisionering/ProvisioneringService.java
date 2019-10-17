package no.nav.tag.sykefravarsstatistikk.api.provisionering;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.domene.Tuple;
import no.nav.tag.sykefravarsstatistikk.api.domene.klassifikasjoner.Sektor;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
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

    public void populerSektorer() {
        List<Sektor> sektorer = datavarehusRepository.hentAlleSektorer();
        Tuple<Integer, Integer> antallOpprettetogOppdatert =
                provisioneringRepository.opprettEllerOppdaterSektorer(sektorer);
        log.info(
                String.format(
                        "Import av sektorer er ferdig. Antall opprettet: %d, antall oppdatert: %d",
                        antallOpprettetogOppdatert.x,
                        antallOpprettetogOppdatert.y
                )
        );
    }


    // TODO: DELETE ME --> bare til versifisering
    public List<Sektor> hentSektorer() {
        return datavarehusRepository.hentAlleSektorer();
    }
}
