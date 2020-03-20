package no.nav.arbeidsgiver.sykefravarsstatistikk.api.altinn;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Fnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.feratureToggles.FeatureToggleService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AltinnService {
    private final AltinnClient altinnClient;
    private final FeatureToggleService featureToggles;

    public AltinnService(AltinnClient altinnClient, FeatureToggleService featureToggles) {
        this.altinnClient = altinnClient;
        this.featureToggles = featureToggles;
    }

    public List<AltinnOrganisasjon> hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(Fnr fnr) {
        if (featureToggles.erEnabled("arbeidsgiver.sykefravarsstatikk-api.bruk-altinn-proxy")) {
            return new ArrayList<>();
        } else {
            return altinnClient.hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(fnr);
        }
    }
}
