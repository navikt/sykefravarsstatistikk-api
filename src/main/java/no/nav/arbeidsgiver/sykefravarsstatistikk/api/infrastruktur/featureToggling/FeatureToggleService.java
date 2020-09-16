package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.featureToggling;

import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeatureToggleService {
    private final Unleash unleash;

    @Autowired
    public FeatureToggleService(Unleash unleash) {
        this.unleash = unleash;
    }

    public Map<String, Boolean> hentFeatureToggles(List<String> features, String sessionId) {
        UnleashContext unleashContext = UnleashContext.builder().sessionId(sessionId).build();

        return features.stream().collect(Collectors.toMap(
                feature -> feature,
                feature -> unleash.isEnabled(feature, unleashContext)
        ));
    }

    public boolean erEnabled(String feature) {
        return unleash.isEnabled(feature);
    }
}
