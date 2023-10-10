package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret

class EnhetsregisteretIkkeTilgjengeligException internal constructor(msg: String?, e: Exception?) :
    RuntimeException(msg, e)
