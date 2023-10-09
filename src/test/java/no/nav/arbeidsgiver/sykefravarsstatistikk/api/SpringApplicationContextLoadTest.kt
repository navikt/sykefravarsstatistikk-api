package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import common.SpringIntegrationTestbase
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api.SykefraværshistorikkController
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SpringApplicationContextLoadTest : SpringIntegrationTestbase() {
    @Autowired
    private val controller: SykefraværshistorikkController? = null
    @Test
    fun contexLoads() {
        Assertions.assertThat(controller).isNotNull()
    }
}
