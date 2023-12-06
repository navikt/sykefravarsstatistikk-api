import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.PrometheusMetrics
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.context.support.beans

val testBeans = beans {
    bean<TomcatServletWebServerFactory>()

    bean<PrometheusMetrics>()
}