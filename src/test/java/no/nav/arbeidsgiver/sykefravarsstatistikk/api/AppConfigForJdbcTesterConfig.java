package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringPerStatistikkKategoriService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringScheduler;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.ImporteringController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.ImporteringScheduler;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.PostImporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.SykefraværsstatistikkImporteringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner.KlassifikasjonsimporteringController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.MockServer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.unleash.UnleashFeatureToggleConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.unleash.UnleashService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.organisasjoner.OrganisasjonerController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.OffentligSykefraværshistorikkController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværshistorikkController;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.AggregertStatistikkService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollUtils;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.jdbc.repository.config.JdbcRepositoryConfigExtension;

/**
 * Med denne configfilen skal Spring kunne sette opp en applikasjon som bare inneholder de
 * nødvendige beans for Jdbc-testing Alt annet (servlet, filters, RestTemplate, ...osv) blir ikke
 * opprettet
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
                UnleashFeatureToggleConfig.class,
                UnleashService.class,
                TilgangskontrollUtils.class,
                EksporteringService.class,
                EksporteringPerStatistikkKategoriService.class,
                EksporteringScheduler.class,
                ImporteringScheduler.class,
                SykefraværsstatistikkImporteringService.class,
                PostImporteringService.class,
                OrganisasjonerController.class,
                ImporteringController.class, EksporteringController.class,
                KlassifikasjonsimporteringController.class,
                SykefraværshistorikkController.class,
                OrganisasjonerController.class,
                TilgangskontrollService.class,
                OffentligSykefraværshistorikkController.class,
                AggregertStatistikkService.class,
            })
    })
public class AppConfigForJdbcTesterConfig extends JdbcRepositoryConfigExtension {

}
