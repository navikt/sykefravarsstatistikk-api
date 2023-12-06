package config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import testBeans

@EnableAutoConfiguration
class FunksjonellSykefraværsstatistikkTestApp : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(applicationContext: GenericApplicationContext) {
        testBeans.initialize(applicationContext)
    }
}

fun main(args: Array<String>) {
    runApplication<FunksjonellSykefraværsstatistikkTestApp>(*args) {
        addInitializers(testBeans)
    }
}
