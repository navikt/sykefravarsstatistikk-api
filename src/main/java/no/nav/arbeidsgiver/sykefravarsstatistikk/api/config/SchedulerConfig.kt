package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor
import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import javax.sql.DataSource

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
open class SchedulerConfig {
    @Bean
    open fun lockProvider(
        @Qualifier("sykefravarsstatistikkDataSource") dataSource: DataSource?
    ): LockProvider {
        return JdbcTemplateLockProvider(dataSource)
    }

    @Bean
    open fun lockingTaskExecutor(lockProvider: LockProvider?): LockingTaskExecutor {
        return DefaultLockingTaskExecutor(lockProvider)
    }
}
