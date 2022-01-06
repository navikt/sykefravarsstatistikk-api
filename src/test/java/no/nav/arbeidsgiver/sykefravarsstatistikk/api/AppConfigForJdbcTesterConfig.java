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
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.jdbc.repository.config.JdbcRepositoryConfigExtension;

/**
 *  Med denne config filen skal Spring kunne sette opp en applikasjon som bare inneholder de nødvendige beans for Jdbc testing
 *  Alt annet (servlet, filters, RestTemplate, ...osv) blir ikke opprettet
 */
@SpringBootConfiguration
@EnableJdbcRepositories({"no.nav.arbeidsgiver"})
@ComponentScan(
        basePackages = {"no.nav.arbeidsgiver"},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        value = {
                                MockServer.class,
                                AltinnClient.class,
                                BesøksstatistikkEventListener.class,
                                UnleashFeatureToggleConfig.class,
                                UnleashService.class,
                                FeatureToggleController.class,
                                TilgangskontrollUtils.class,
                                EksporteringService.class,
                                EksporteringScheduler.class,
                                ImporteringScheduler.class,
                                ImporteringService.class,
                                PostImporteringService.class,
                                OrganisasjonerController.class,
                                ImporteringController.class, EksporteringController.class,
                                KlassifikasjonsimporteringController.class,
                                SykefraværshistorikkController.class,
                                BedriftsmetrikkerController.class,
                                OrganisasjonerController.class,
                                TilgangskontrollService.class
                        })
        })
public class AppConfigForJdbcTesterConfig extends JdbcRepositoryConfigExtension {
}
