package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
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
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.sql.DataSource;

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
        return dataSource("admin");
    }

    @Bean(name = "sykefravarsstatistikkJdbcTemplate")
    public NamedParameterJdbcTemplate sykefravarsstatistikkJdbcTemplate(
            @Qualifier("sykefravarsstatistikkDataSource") DataSource dataSource
    ) {
        return new NamedParameterJdbcTemplate(dataSource);
    }


    //@SneakyThrows
    private HikariDataSource dataSource(String user) {
        logger.info(String.format("Vmp? '%s'", mountPath));
        logger.info(String.format("Hikari DS URL? '%s'", databaseUrl));
        logger.info(String.format("Hikari DS navn? '%s'", databaseNavn));

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

        logger.info(String.format("[GCP-migrering] Er HikariDataSource klar? %b", hikariDataSourceWithVaultIntegration != null));

        if (hikariDataSourceWithVaultIntegration != null) {
            logger.info(String.format("[GCP-migrering] Har vi en HikariDataSource? %b", hikariDataSourceWithVaultIntegration.getDataSource() != null));
            logger.info(String.format("[GCP-migrering] Har vi HikariConfigMXBean? %s", hikariDataSourceWithVaultIntegration.getHikariConfigMXBean() != null));
            if (hikariDataSourceWithVaultIntegration.getHikariConfigMXBean() != null) {
                logger.info(String.format("[GCP-migrering] Har vi HikariConfigMXBean? %s", hikariDataSourceWithVaultIntegration.getHikariConfigMXBean().getPoolName()));
            }
        }

        return hikariDataSourceWithVaultIntegration;
    }

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        logger.info("[GCP-migrering] oppretter FlywayMigrationStrategy");
        return flyway -> Flyway.configure()
                .dataSource(dataSource("admin"))
                .initSql(String.format("SET ROLE \"%s\"", dbRole("admin")))
                .load()
                .migrate();
    }

    private String dbRole(String role) {
        return String.join("-", databaseNavn, role);
    }

}
