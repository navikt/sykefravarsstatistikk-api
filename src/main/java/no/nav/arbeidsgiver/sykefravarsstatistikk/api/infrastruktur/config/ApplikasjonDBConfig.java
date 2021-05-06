package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

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


    @SneakyThrows
    private HikariDataSource dataSource(String user) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("Sykefraværsstatistikk-connection-pool");
        config.setJdbcUrl(databaseUrl);
        config.setMaximumPoolSize(8);
        config.setMinimumIdle(1);
        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, mountPath, dbRole(user));
    }

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
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
