package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

@Configuration
@Profile("local", "mvc-test", "db-test")
open class ApplikasjonDBConfigLocal(private val environment: Environment) {
    @Value("\${applikasjon.datasource.url}")
    private val databaseUrl: String? = null

    @Value("\${applikasjon.datasource.username}")
    private val username: String? = null

    @Value("\${applikasjon.datasource.password}")
    private val password: String? = null

    @Value("\${applikasjon.datasource.driver-class-name}")
    private val driverClassName: String? = null
    @Primary
    @Bean(name = ["sykefravarsstatistikkDataSource"])
    open fun sykefravarsstatistikkDataSource(): DataSource {
        val config = HikariConfig()
        config.poolName = "sykefraværsstatistikk-connection-pool-local"
        config.jdbcUrl = databaseUrl
        config.username = username
        config.password = password
        config.maximumPoolSize = 2
        config.driverClassName = driverClassName
        config.connectionTimeout = 3000
        return HikariDataSource(config)
    }

    @Primary
    @Bean(name = ["sykefravarsstatistikkJdbcTemplate"])
    open fun sykefravarsstatistikkJdbcTemplate(
        @Qualifier("sykefravarsstatistikkDataSource") dataSource: DataSource
    ): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource)
    }

    @Bean(name = ["sykefravarsstatistikkDatabase"])
    open fun database(
        @Qualifier("sykefravarsstatistikkDataSource") dataSource: DataSource,
    ): Database {
        val db = Database.connect(dataSource)
        TransactionManager.defaultDatabase = db
        return db
    }

    @Bean
    open fun flywayMigrationStrategy(): FlywayMigrationStrategy {
        val locations: MutableList<String> = ArrayList()
        locations.add("/db/migration")
        locations.add("/db/test-datavarehus")
        val profiles = environment.activeProfiles
        if (listOf<String>(*profiles).contains("mvc-test") || listOf<String>(*profiles)
                .contains("local")
        ) {
            locations.add("/db/test-lokaldb-data")
        }
        return FlywayMigrationStrategy {
            Flyway.configure()
                .dataSource(sykefravarsstatistikkDataSource())
                .locations(*locations.toTypedArray<String>())
                .load()
                .migrate()
        }
    }
}
