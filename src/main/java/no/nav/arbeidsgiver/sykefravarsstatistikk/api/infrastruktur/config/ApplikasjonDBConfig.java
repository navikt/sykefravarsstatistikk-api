package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil;
import no.nav.vault.jdbc.hikaricp.VaultError;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

import static java.lang.String.format;

@Configuration
@Profile({"dev", "prod"})
public class ApplikasjonDBConfig {

    // URL hentes i Vault
    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${database.navn}")
    private String databaseNavn;

    @Value("${vault.mount-path}")
    private String mountPath;

    private static Logger logger = LoggerFactory.getLogger(ApplikasjonDBConfig.class);


    @Bean(name = "sykefravarsstatistikkDataSource")
    public DataSource userDataSource() {
        logger.info(format("userDataSource() - Oppretter datasource, URL er: '%s'", databaseUrl));
        logger.info(format("userDataSource() - Oppretter datasource, URL er: '%s'", databaseNavn));

        return dataSource("admin");
    }

    @Bean(name = "sykefravarsstatistikkJdbcTemplate")
    public NamedParameterJdbcTemplate sykefravarsstatistikkJdbcTemplate(
            @Qualifier("sykefravarsstatistikkDataSource") DataSource dataSource
    ) {
        logger.info(format("sykefravarsstatistikkJdbcTemplate() - Oppretter datasource, URL er: '%s'", databaseUrl));
        logger.info(format("sykefravarsstatistikkJdbcTemplate() - Oppretter datasource, URL er: '%s'", databaseNavn));

        return new NamedParameterJdbcTemplate(dataSource);
    }


    private HikariDataSource dataSource(String user) {
        System.out.println("[DEBUG] ----> oppretter datasource");
        logger.info(format("dataSource() - Oppretter datasource, URL er: '%s'", databaseUrl));
        logger.info(format("dataSource() - Oppretter datasource, URL er: '%s'", databaseNavn));

        HikariConfig config = new HikariConfig();
        config.setPoolName("Sykefraværsstatistikk-connection-pool");
        config.setJdbcUrl(databaseUrl);
        config.setMaximumPoolSize(8);
        config.setMinimumIdle(1);
        HikariDataSource hikariDataSourceWithVaultIntegration = null;
        try {
            hikariDataSourceWithVaultIntegration = HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, mountPath, dbRole(user));
        } catch (VaultError vaultError) {
            logger.warn("[GCP-migrering] Kunne ikke opprette DS. Returnerer null. ", vaultError);
            return null;
        }

        return hikariDataSourceWithVaultIntegration;
    }

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> Flyway.configure()
                .dataSource(dataSource("admin"))
                .initSql(format("SET ROLE \"%s\"", dbRole("admin")))
                .load()
                .migrate();
    }

    private String dbRole(String role) {
        return String.join("-", databaseNavn, role);
    }

}
