package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret

class EnhetsregisteretMappingException : RuntimeException {
    internal constructor(msg: String?, e: Exception?) : super(msg, e)
    internal constructor(msg: String?) : super(msg)
}
