package no.nav.tag.sykefravarsstatistikk.api.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.sammenligning.SammenligningService;
import org.springframework.stereotype.Component;

@Component
public class Query implements GraphQLQueryResolver {

    private final SammenligningService sammenligningService;


    public Query(SammenligningService sammenligningService) {
        this.sammenligningService = sammenligningService;
    }

    public Sammenligning getSammenligning(final String orgnr) {
        return sammenligningService.hentSammenligningForUnderenhet(new Orgnr(orgnr));
    }
}
