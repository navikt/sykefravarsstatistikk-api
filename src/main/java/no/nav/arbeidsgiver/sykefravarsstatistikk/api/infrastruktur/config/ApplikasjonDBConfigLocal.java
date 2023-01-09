package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@Profile({"local", "mvc-test", "db-test"})
public class ApplikasjonDBConfigLocal {

  @Value("${applikasjon.datasource.url}")
  private String databaseUrl;

  @Value("${applikasjon.datasource.username}")
  private String username;

  @Value("${applikasjon.datasource.password}")
  private String password;

  @Value("${applikasjon.datasource.driver-class-name}")
  private String driverClassName;

  private final Environment environment;

  public ApplikasjonDBConfigLocal(Environment environment) {
    this.environment = environment;
  }

  @Primary
  @Bean(name = "sykefravarsstatistikkDataSource")
  public DataSource sykefravarsstatistikkDataSource() {
    HikariConfig config = new HikariConfig();
    config.setPoolName("Sykefraværsstatistikk-connection-pool-local");
    config.setJdbcUrl(databaseUrl);
    config.setUsername(username);
    config.setPassword(password);
    config.setMaximumPoolSize(2);
    config.setDriverClassName(driverClassName);

    return new HikariDataSource(config);
  }

  @Primary
  @Bean(name = "sykefravarsstatistikkJdbcTemplate")
  public NamedParameterJdbcTemplate sykefravarsstatistikkJdbcTemplate(
      @Qualifier("sykefravarsstatistikkDataSource") DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }

  @Bean
  public FlywayMigrationStrategy flywayMigrationStrategy() {
    List<String> locations = new ArrayList<>();
    locations.add("/db/migration");
    locations.add("/db/test-datavarehus");

    String[] profiles = environment.getActiveProfiles();
    if (Arrays.asList(profiles).contains("mvc-test") || Arrays.asList(profiles).contains("local")) {
      locations.add("/db/test-lokaldb-data");
    }

    return flyway -> {
      Flyway.configure()
          .dataSource(sykefravarsstatistikkDataSource())
          .locations(locations.toArray(new String[0]))
          .load()
          .migrate();
    };
  }
}
