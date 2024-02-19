package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

@Configuration
@Profile("compose")
open class DatabaseConfig {
	@Bean
	@Primary
	@ConfigurationProperties("spring.applikasjon.datasource.hikari")
	open fun applikasjonDatasourceProps() = DataSourceProperties()

	@Bean
	@ConfigurationProperties("spring.datavarehus.datasource.hikari")
	open fun datavarehusDatasourceProps() = DataSourceProperties()

	@Bean
	@Primary
	open fun sykefravarsstatistikkDataSource() : DataSource =
		applikasjonDatasourceProps().initializeDataSourceBuilder().build()

	@Bean
	open fun datavarehusDataSource() : DataSource =
		datavarehusDatasourceProps().initializeDataSourceBuilder().build()

	@Bean
	open fun datavarehusJdbcTemplate(
		@Qualifier("datavarehusDataSource") dataSource: DataSource
	): NamedParameterJdbcTemplate {
		return NamedParameterJdbcTemplate(dataSource)
	}

	@Bean
	open fun sykefravarsstatistikkDatabase(
		@Qualifier("sykefravarsstatistikkDataSource") dataSource: DataSource,
	): Database {
		val db = Database.connect(dataSource)
		TransactionManager.defaultDatabase = db
		return db
	}

	@Bean
	open fun datavarehusDatabase(
		@Qualifier("datavarehusDataSource") dataSource: DataSource,
	): Database {
		val db = Database.connect(dataSource)
		TransactionManager.defaultDatabase = db
		return db
	}

	@Bean
	open fun flywayMigrationStrategy(
		@Qualifier("sykefravarsstatistikkDataSource") applikasjonDataSource: DataSource,
		@Qualifier("datavarehusDataSource") datavarehusDataSource: DataSource
	): FlywayMigrationStrategy {
		return FlywayMigrationStrategy {
			// appliksjonsmigrering
			Flyway.configure()
				.dataSource(applikasjonDataSource)
				.locations("/db/migration")
				.load()
				.migrate()

			// datavarehusmigrering
			Flyway.configure()
				.dataSource(datavarehusDataSource)
				.locations("/db/test-datavarehus")
				.load()
				.migrate()

		}
	}
}