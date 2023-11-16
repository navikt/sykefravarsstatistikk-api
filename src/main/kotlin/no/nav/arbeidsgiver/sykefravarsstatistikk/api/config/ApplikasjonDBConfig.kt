package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import no.nav.vault.jdbc.hikaricp.VaultError
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

@Configuration
@Profile("dev", "prod")
open class ApplikasjonDBConfig {
    // URL hentes i Vault
    @Value("\${spring.datasource.url}")
    private val databaseUrl: String? = null

    @Value("\${database.navn}")
    private val databaseNavn: String? = null

    @Value("\${vault.mount-path}")
    private val mountPath: String? = null
    @Primary
    @Bean(name = ["sykefravarsstatistikkDataSource"])
    open fun userDataSource(): DataSource? {
        return dataSource("admin")
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

    private fun dataSource(user: String): HikariDataSource? {
        val config = HikariConfig()
        config.poolName = "Sykefraværsstatistikk-connection-pool"
        config.jdbcUrl = databaseUrl
        config.maximumPoolSize = 8
        config.minimumIdle = 1
        val hikariDataSourceWithVaultIntegration: HikariDataSource? = try {
            HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(
                config, mountPath, dbRole(user)
            )
        } catch (vaultError: VaultError) {
            logger.warn("[GCP-migrering] Kunne ikke opprette DS. Returnerer null. ", vaultError)
            return null
        }
        return hikariDataSourceWithVaultIntegration
    }

    @Bean
    open fun flywayMigrationStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy {
            Flyway.configure()
                .dataSource(dataSource("admin"))
                .initSql(String.format("SET ROLE \"%s\"", dbRole("admin")))
                .load()
                .migrate()
        }
    }

    private fun dbRole(role: String): String {
        return java.lang.String.join("-", databaseNavn, role)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ApplikasjonDBConfig::class.java)
    }
}
