package no.nav.tag.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class TapteDagsverkService {

    private final int sisteÅrstall;
    private final int sisteKvartal;
    private final TapteDagsverkForKostnadsberegningRepository tapteDagsverkForKostnadsberegningRepository;

    public TapteDagsverkService(
            @Value("${statistikk.import.siste.arstall}") int sisteÅrstall,
            @Value("${statistikk.import.siste.arstall}") int sisteKvartal,
            TapteDagsverkForKostnadsberegningRepository tapteDagsverkForKostnadsberegningRepository) {
        this.sisteÅrstall = sisteÅrstall;
        this.sisteKvartal = sisteKvartal;
        this.tapteDagsverkForKostnadsberegningRepository = tapteDagsverkForKostnadsberegningRepository;
    }

    public List<TapteDagsverk> hentTapteDagsverkFraDeSiste4Kvartalene(Underenhet underenhet) {
        return tapteDagsverkForKostnadsberegningRepository.hentTapteDagsverkFor4Kvartaler(hentDe4SisteTilgjengeligeKvartalene(), underenhet);
    }

    private List<ÅrstallOgKvartal> hentDe4SisteTilgjengeligeKvartalene() {
        return Arrays.asList(
                new ÅrstallOgKvartal(sisteÅrstall, sisteKvartal),
                new ÅrstallOgKvartal(sisteÅrstall, sisteKvartal).minusKvartaler(1),
                new ÅrstallOgKvartal(sisteÅrstall, sisteKvartal).minusKvartaler(2),
                new ÅrstallOgKvartal(sisteÅrstall, sisteKvartal).minusKvartaler(3)
        );
    }

}
