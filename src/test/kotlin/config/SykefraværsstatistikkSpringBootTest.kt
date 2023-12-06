package config

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(
    classes = [FunksjonellSykefraværsstatistikkTestApp::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(initializers = [FunksjonellSykefraværsstatistikkTestApp::class])
@AutoConfigureMockMvc
@Target(AnnotationTarget.CLASS)
annotation class SykefraværsstatistikkSpringBootTest
