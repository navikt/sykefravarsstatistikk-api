package no.nav.tag.sykefravarsstatistikk.api.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
@Profile({"local", "dev", "prod"})
public class DatavarehusDBConfig {

    @Bean(name = "datavarehusDS")
    @ConfigurationProperties("datavarehus.datasource")
    public DataSource datavarehusDataSource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create().type(BasicDataSource.class);
        return dataSourceBuilder.build();
    }

    @Bean(name = "datavarehusJdbcTemplate")
    public NamedParameterJdbcTemplate datavarehusJdbcTemplate(@Qualifier("datavarehusDS") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

}
