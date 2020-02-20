package no.nav.tag.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class TapteDagsverkService {

    private final int sisteÅrstall;
    private final int sisteKvartal;
    private final TapteDagsverkForKostnadsberegningRepository tapteDagsverkForKostnadsberegningRepository;

    public TapteDagsverkService(
            @Value("${statistikk.import.siste.arstall}") int sisteÅrstall,
            @Value("${statistikk.import.siste.kvartal}") int sisteKvartal,
            TapteDagsverkForKostnadsberegningRepository tapteDagsverkForKostnadsberegningRepository) {
        this.sisteÅrstall = sisteÅrstall;
        this.sisteKvartal = sisteKvartal;
        this.tapteDagsverkForKostnadsberegningRepository = tapteDagsverkForKostnadsberegningRepository;
    }

    public TapteDagsverk hentOgSummerTapteDagsverk(Orgnr orgnr) {
        return summTapteDagsverk(
                hentTapteDagsverkFraDeSiste4Kvartalene(orgnr)
        );
    }

    public List<KvartalsvisTapteDagsverk> hentTapteDagsverkFraDeSiste4Kvartalene(Orgnr orgnr) {
        return tapteDagsverkForKostnadsberegningRepository.hentTapteDagsverkFor4Kvartaler(hentDe4SisteTilgjengeligeKvartalene(), orgnr);
    }

    private TapteDagsverk summTapteDagsverk(List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverksListe) {

        if (kvartalsvisTapteDagsverksListe.size() != 4 ||
                kvartalsvisTapteDagsverksListe.stream().anyMatch(kvartalsvisTapteDagsverk -> kvartalsvisTapteDagsverk.isErMaskert())) {
            return new TapteDagsverk(new BigDecimal(0), true);
        }
        return new TapteDagsverk(kvartalsvisTapteDagsverksListe
                .stream()
                .map(KvartalsvisTapteDagsverk::getTapteDagsverk)
                .reduce(BigDecimal.ZERO, BigDecimal::add), false);
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
