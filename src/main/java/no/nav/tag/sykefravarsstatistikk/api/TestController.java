package no.nav.tag.sykefravarsstatistikk.api;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.security.oidc.api.Unprotected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Unprotected
public class TestController {

    private final MeterRegistry meterRegistry;

    public TestController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        DistributionSummary histogram = DistributionSummary
                .builder("tall-histogram")
                .publishPercentiles(0.25, 0.5, 0.75)
                .publishPercentileHistogram()
                .minimumExpectedValue(1L)
                .register(meterRegistry);

    }

    @GetMapping(value = "/test/{tall}")
    public String test(
            @PathVariable("tall") int tall
    ) {

        histogram(9);
        histogram(11);
        histogram(18);
        histogram(50);
        histogram(60);
        histogram(125);
        histogram(234);
        histogram(734);
        histogram(10012);
        histogram(124613);
        return "tall: " + tall;
    }

    private void histogram(int tall) {
        meterRegistry.summary("tall-histogram").record(tall);
    }
}
