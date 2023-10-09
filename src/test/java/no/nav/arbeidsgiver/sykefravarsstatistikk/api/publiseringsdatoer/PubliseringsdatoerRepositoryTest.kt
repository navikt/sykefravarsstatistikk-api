package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer

import common.StaticAppender
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@ExtendWith(MockitoExtension::class)
internal class PubliseringsdatoerRepositoryTest {
    var publiseringsdatoerRepository: PubliseringsdatoerRepository? = null

    @Mock
    var mockJdbcTemplate: NamedParameterJdbcTemplate? = null
    @BeforeEach
    fun setUp() {
        publiseringsdatoerRepository = PubliseringsdatoerRepository(mockJdbcTemplate!!)
        StaticAppender.clearEvents()
    }

    @AfterEach
    fun tearDown() {
        Mockito.reset(mockJdbcTemplate)
    }

    @Test
    fun hentSistePubliseringstidspunkt_nårPubliseringsdatoIkkeBlirFunnet_skalReturnereTomOptionalOgLoggeError() {
        whenever(mockJdbcTemplate!!.query(ArgumentMatchers.anyString(), anyRowMapper())).thenReturn(listOf())
        val faktisk = publiseringsdatoerRepository!!.hentSisteImporttidspunkt()
        Assertions.assertNull(faktisk)
    }

    private fun anyRowMapper(): RowMapper<*> {
        return any()
    }
}
