package infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sykefraværsstatistikk
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.SlettOgOpprettResultat.Companion.tomtResultat
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.BatchCreateSykefraværsstatistikkFunction
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.DeleteSykefraværsstatistikkFunction
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.StatistikkRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværsstatistikkIntegrasjonUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.math.BigDecimal
import java.util.stream.IntStream

class StatistikkRepositoryTest {
    private val jdbcTemplate: NamedParameterJdbcTemplate = mock()
    private val statistikkRepository: StatistikkRepository = StatistikkRepository(jdbcTemplate)

    @Test
    fun importStatistikk__skal_ikke_slette_eksisterende_statistikk_når_det_ikke_er_noe_data_å_importere() {
        val resultat = statistikkRepository.importStatistikk(
            "Test stats", emptyList(),
            ÅrstallOgKvartal(2019, 3),
            integrasjonUtils
        )
        AssertionsForClassTypes.assertThat(resultat).isEqualTo(tomtResultat())
    }

    @Test
    fun batchOpprett__deler_import_i_små_batch() {
        val list = getSykefraværsstatistikkVirksomhetList(5)
        val resultat = statistikkRepository.batchOpprett(list, dummyUtils(), 2)
        AssertionsForClassTypes.assertThat(resultat).isEqualTo(5)
    }

    @Test
    fun batchOpprett__ikke_deler_dersom_batch_størrelse_er_større_enn_listen() {
        val list = getSykefraværsstatistikkVirksomhetList(5)
        val resultat = statistikkRepository.batchOpprett(list, dummyUtils(), 1000)
        AssertionsForClassTypes.assertThat(resultat).isEqualTo(5)
    }

    companion object {
        private fun getSykefraværsstatistikkVirksomhetList(
            antallStatistikk: Int
        ): List<SykefraværsstatistikkVirksomhet> {
            val list: MutableList<SykefraværsstatistikkVirksomhet> = ArrayList()
            IntStream.range(0, antallStatistikk)
                .forEach { i: Int -> list.add(sykefraværsstatistikkVirksomhet(2000 + i, 1)) }
            return list
        }

        private fun sykefraværsstatistikkVirksomhet(
            årstall: Int, kvartal: Int
        ): SykefraværsstatistikkVirksomhet {
            return SykefraværsstatistikkVirksomhet(
                årstall,
                kvartal,
                "987654321",
                Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
                10,
                BigDecimal(15),
                BigDecimal(450)
            )
        }

        private val integrasjonUtils: SykefraværsstatistikkIntegrasjonUtils
            get() = object : SykefraværsstatistikkIntegrasjonUtils {
                override fun getDeleteFunction(): DeleteSykefraværsstatistikkFunction {
                    return DeleteSykefraværsstatistikkFunction {
                        throw IllegalStateException(
                            "Skal ikke bruke delete funksjon"
                        )
                    }
                }

                override fun getBatchCreateFunction(
                    statistikk: List<Sykefraværsstatistikk>
                ): BatchCreateSykefraværsstatistikkFunction {
                    TODO()
                }
            }

        private fun dummyUtils(): SykefraværsstatistikkIntegrasjonUtils {
            return object : SykefraværsstatistikkIntegrasjonUtils {
                override fun getDeleteFunction(): DeleteSykefraværsstatistikkFunction {
                    TODO()
                }

                override fun getBatchCreateFunction(
                    statistikk: List<Sykefraværsstatistikk>
                ): BatchCreateSykefraværsstatistikkFunction {
                    return BatchCreateSykefraværsstatistikkFunction { statistikk.size }
                }
            }
        }
    }
}
