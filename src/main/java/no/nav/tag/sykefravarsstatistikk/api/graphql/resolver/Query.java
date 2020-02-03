package no.nav.tag.sykefravarsstatistikk.api.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.sammenligning.SammenligningService;
import no.nav.tag.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning.TapteDagsverk;
import no.nav.tag.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning.TapteDagsverkService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Query implements GraphQLQueryResolver {

    private final SammenligningService sammenligningService;
    private final TapteDagsverkService tapteDagsverkService;


    public Query(SammenligningService sammenligningService, TapteDagsverkService tapteDagsverkService) {
        this.sammenligningService = sammenligningService;
        this.tapteDagsverkService = tapteDagsverkService;
    }

    public Sammenligning getSammenligning(final String orgnr) {
        // check rettigheter
        return sammenligningService.hentSammenligningForUnderenhet(new Orgnr(orgnr));
    }

    public List<TapteDagsverk> getTapteDagsverk(final String orgnr) {
        return tapteDagsverkService.hentTapteDagsverkFraDeSiste4Kvartalene(new Orgnr(orgnr));
    }
}
