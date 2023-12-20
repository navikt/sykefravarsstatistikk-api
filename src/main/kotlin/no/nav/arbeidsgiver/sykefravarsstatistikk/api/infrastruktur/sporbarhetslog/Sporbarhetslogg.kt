package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.sporbarhetslog

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.CorrelationIdFilter
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.MarkerFactory
import org.springframework.stereotype.Component

@Component
class Sporbarhetslogg {
    private val sporbarhetslogger = LoggerFactory.getLogger(this::class.java)
    private val secureMarker = MarkerFactory.getMarker("SECURE_LOG")

    fun loggHendelse(event: Loggevent, kommentar: String?) {
        val extensions = getExtensions(event)
        extensions.add("cs5=$kommentar")
        extensions.add("cs5Label=Kommentar")
        val loggmelding = lagLoggmelding(extensions, event.harTilgang)
        if (event.harTilgang) {
            sporbarhetslogger.info(secureMarker, loggmelding)
        } else {
            sporbarhetslogger.warn(secureMarker, loggmelding)
        }
    }

    fun loggHendelse(event: Loggevent) {
        val loggmelding = lagLoggmelding(getExtensions(event), event.harTilgang)
        if (event.harTilgang) {
            sporbarhetslogger.info(secureMarker, loggmelding)
        } else {
            sporbarhetslogger.warn(secureMarker, loggmelding)
        }
    }

    private fun lagLoggmelding(extensions: List<String>, harTilgang: Boolean): String {
        val version = "CEF:0"
        val deviceVendor = "sykefravarsstatistikk-api"
        val deviceProduct = "sporbarhetslogg"
        val deviceVersion = "1.0"
        val signatureID = "sykefravarsstatistikk-api:accessed"
        val name = "sykefravarsstatistikk"
        val severity = if (harTilgang) "INFO" else "WARN"
        val extension = java.lang.String.join(" ", extensions)
        return java.lang.String.join(
            "|",
            listOf(
                version,
                deviceVendor,
                deviceProduct,
                deviceVersion,
                signatureID,
                name,
                severity,
                extension
            )
        )
    }

    private fun getExtensions(event: Loggevent): MutableList<String> {
        val extensions: MutableList<String> = ArrayList()
        extensions.add("end=" + System.currentTimeMillis())
        extensions.add("suid=" + event.innloggetBruker!!.fnr.verdi)
        extensions.add("request=" + event.requestUrl)
        extensions.add("requestMethod=" + event.requestMethod)
        extensions.add("cs3=" + if (event.orgnr == null) "" else event.orgnr.verdi)
        extensions.add("cs3Label=OrgNr")
        val decision = if (event.harTilgang) "Permit" else "Deny"
        extensions.add("flexString1=$decision")
        extensions.add("flexString1Label=Decision")
        if (!event.harTilgang) {
            extensions.add("flexString2=Bruker har ikke rettighet i Altinn")
            extensions.add("flexString2Label=Begrunnelse")
            extensions.add("cn1=" + event.altinnServiceCode)
            extensions.add("cn1Label=Service Code")
            extensions.add("cn2=" + event.altinnServiceEdition)
            extensions.add("cn2Label=Service Edition")
        }
        extensions.add("sproc=" + MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_NAME))
        return extensions
    }
}
