package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.hash

import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom

@Component
class Sha3Hasher : Hasher {
    private val messageDigest get() = MessageDigest.getInstance("SHA3-256")
    private val charset = Charsets.UTF_8

    override fun hash(data: String, salt: ByteArray): ByteArray {
        val hashedData = messageDigest.apply {
            update(data.toByteArray(charset))
            update(salt)
        }.digest()

        return hashedData
    }

    override fun generateRandomSalt(): ByteArray {
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        return salt
    }
}

