
import io.prometheus.client.CollectorRegistry
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.PrometheusMetrics
import org.springframework.context.support.beans

val testBeans = beans {
    bean<PrometheusMetrics>()
    bean<CollectorRegistry>()
}