package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.hash

interface Hasher {
    fun hash(data: String, salt: ByteArray): ByteArray
    fun generateRandomSalt(): ByteArray
}
