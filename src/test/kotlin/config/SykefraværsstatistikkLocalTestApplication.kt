package config

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import testBeans

class SykefraværsstatistikkLocalTestApplication: ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(applicationContext: GenericApplicationContext) {
       testBeans.initialize(applicationContext)
    }
}
