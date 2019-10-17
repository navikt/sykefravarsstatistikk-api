package no.nav.tag.sykefravarsstatistikk.api.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
@Profile({"local"})
public class ApplikasjonDBConfigLocal {

    @Value("${applikasjon.datasource.url}")
    private String databaseUrl;


    @Bean(name = "applikasjonDS")
    @ConfigurationProperties("applikasjon.datasource")
    public DataSource springDataSource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create().type(BasicDataSource.class);
        DataSource dataSource = dataSourceBuilder.build();
        return dataSource;
    }

    @Bean(name = "applikasjonJdbcTemplate")
    public NamedParameterJdbcTemplate springJdbcTemplate(@Qualifier("applikasjonDS") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> Flyway.configure()
                .dataSource(springDataSource())
                .locations("/db/migration", "/db/test-lokaldb-data", "/db/test-datavarehus")
                .load()
                .migrate();
    }

}
