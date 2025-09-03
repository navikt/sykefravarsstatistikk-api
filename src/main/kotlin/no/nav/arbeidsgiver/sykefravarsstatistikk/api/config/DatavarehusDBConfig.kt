package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*
import javax.sql.DataSource
import org.slf4j.LoggerFactory

@Configuration
@Profile("!compose")
open class DatavarehusDBConfig {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Value("\${datavarehus.datasource.url}")
    private val databaseUrl: String? = null

    @Value("\${datavarehus.datasource.username}")
    private val username: String? = null

    @Value("\${datavarehus.datasource.password}")
    private val password: String? = null

    @Value("\${datavarehus.datasource.driver-class-name}")
    private val driverClassName: String? = null
    @Bean(name = ["datavarehusDS"])
    open fun datavarehusDataSource(): DataSource {
        log.info("Creating datavarehusDataSource på url: '$databaseUrl'")
        val properties = Properties()
        properties["dataSource.oracle.jdbc.fanEnabled"] = false
        val config = HikariConfig(properties)
        config.poolName = "Datavarehus-connection-pool"
        config.jdbcUrl = databaseUrl
        config.username = username
        config.password = password
        config.maximumPoolSize = 2
        config.driverClassName = driverClassName
        return HikariDataSource(config)
    }

    @Bean(name = ["datavarehusJdbcTemplate"])
    open fun datavarehusJdbcTemplate(
        @Qualifier("datavarehusDS") dataSource: DataSource
    ): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource)
    }

    @Bean(name = ["datavarehusDatabase"])
    open fun datavarehusDatabase(
        @Qualifier("datavarehusDS") dataSource: DataSource
    ): Database {
        val db = Database.connect(dataSource)
        TransactionManager.defaultDatabase = db
        return db
    }
}
