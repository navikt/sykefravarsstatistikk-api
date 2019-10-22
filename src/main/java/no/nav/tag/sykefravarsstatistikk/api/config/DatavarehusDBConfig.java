package no.nav.tag.sykefravarsstatistikk.api.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Properties;

@Slf4j
@Configuration
@Profile({"local", "dev", "prod"})
public class DatavarehusDBConfig {

    @Value("${datavarehus.datasource.url}")
    private String databaseUrl;

    @Value("${datavarehus.datasource.username}")
    private String username;

    @Value("${datavarehus.datasource.password}")
    private String password;

    @Value("${datavarehus.datasource.driver-class-name}")
    private String driverClassName;


    @Bean(name = "datavarehusDS")
    public DataSource datavarehusDataSource() {
        // TODO: DELETE ME!
        startsWithOrBlank("property1", username);
        startsWithOrBlank("property2", password);

        Properties properties = new Properties();
        properties.put("dataSource.oracle.jdbc.fanEnabled", false);
        HikariConfig config = new HikariConfig(properties);
        config.setJdbcUrl(databaseUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(2);
        config.setDriverClassName(driverClassName);

        return new HikariDataSource(config);
    }

    @Bean(name = "datavarehusJdbcTemplate")
    public NamedParameterJdbcTemplate datavarehusJdbcTemplate(@Qualifier("datavarehusDS") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }


    private void startsWithOrBlank(String name, String value) {
        if (StringUtils.isBlank(value)) {
            log.info(String.format("%s is blank", name));
        } else {
            log.info(String.format("%s is %d chars long and starts w/: %s", name, value.length(), value.substring(0, 1)));
        }
    }
}
