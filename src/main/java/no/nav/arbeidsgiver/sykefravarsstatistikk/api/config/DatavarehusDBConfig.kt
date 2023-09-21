package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*
import javax.sql.DataSource

@Configuration
open class DatavarehusDBConfig {
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
}
