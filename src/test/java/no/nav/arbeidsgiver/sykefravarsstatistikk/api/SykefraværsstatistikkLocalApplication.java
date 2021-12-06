package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringScheduler;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.ImporteringController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.ImporteringScheduler;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.ImporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.PostImporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner.KlassifikasjonsimporteringController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.MockServer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.LocalOgUnitTestOidcConfiguration;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.featuretoggling.FeatureToggleController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.unleash.UnleashFeatureToggleConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.unleash.UnleashService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.BedriftsmetrikkerController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk.BesøksstatistikkEventListener;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.organisasjoner.OrganisasjonerController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværshistorikkController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollUtils;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile({"local", "mvc-test"})
@SpringBootApplication
@ComponentScan(
        basePackages = {"no.nav.arbeidsgiver"},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        value = {
                                AppConfigForJdbcTesterConfig.class,
                        })
        })
@EnableJwtTokenValidation(ignore = {"org.springframework", "org.springdoc"})
@EnableConfigurationProperties
@PropertySource("application-local.yaml")
public class SykefraværsstatistikkLocalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SykefraværsstatistikkLocalApplication.class, args);
    }
}
