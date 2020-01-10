package no.nav.tag.sykefravarsstatistikk.api.bransjeprogram;

import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.KlassifikasjonerRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BransjeService {
    private final KlassifikasjonerRepository klassifikasjonerRepository;

    public BransjeService(KlassifikasjonerRepository klassifikasjonerRepository) {
        this.klassifikasjonerRepository = klassifikasjonerRepository;
    }

    public List<String> hentNæringskoder5sifferTilhørendeBransje(Bransje bransje) {
        List<String> næringskoder5siffer = new ArrayList<>();

        bransje.getKoderSomSpesifisererNæringer().forEach(kode -> {
            if (kode.length() == 5) {
                næringskoder5siffer.add(kode);
            } else if (kode.length() == 2) {
                klassifikasjonerRepository.hentNæringsgrupperingerTilhørendeNæringskode2siffer(kode)
                        .forEach(næringsgruppering -> næringskoder5siffer.add(næringsgruppering.getKode5siffer()));
            } else {
                throw new IllegalArgumentException("Støtter ikke bransjer som ikke er spesifisert av enten 2 eller 5 sifre");
            }
        });

        return næringskoder5siffer;
    }
}
