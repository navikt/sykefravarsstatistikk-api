package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.hash.Sha3Hasher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class Sha3HasherTest {
    private val hasher = Sha3Hasher()

    @Test
    fun `hasher should always hash to the same value given the same salt`() {
        val data = "Data to be hashed"
        val salt = "Salt".toByteArray()
        val result = hasher.hash(data, salt)

        assertThat(result).isEqualTo(
            "4ce65935eb85af6941f49702d764b516ce12284b113f48871cfb76b763e1634a".hexStringToByteArray()
        )
    }

    @Test
    fun `hasher should always hash to a different value given different salts`() {
        val data = "Data to be hashed"
        val firstSalt = hasher.generateRandomSalt()
        val secondSalt = hasher.generateRandomSalt()
        val firstResult = hasher.hash(data, firstSalt)
        val secondResult = hasher.hash(data, secondSalt)

        assertThat(firstResult).isNotEqualTo(secondResult)
    }

    private fun String.hexStringToByteArray(): ByteArray {
        return chunked(2)
            .map { it.toIntOrNull(16) ?: 0 }
            .map { it.toByte() }
            .toByteArray()
    }
}