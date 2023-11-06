package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import com.google.common.collect.Lists
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.SlettOgOpprettResultat
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.SlettOgOpprettResultat.Companion.tomtResultat
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Component
class StatistikkRepository(
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
) {
    val INSERT_BATCH_STØRRELSE = 10000

    private val log = LoggerFactory.getLogger(this::class.java)

    fun importSykefraværsstatistikkNæringMedVarighet(
        sykefraværsstatistikkNæringMedVarighet: List<SykefraværsstatistikkNæringMedVarighet>,
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        if (sykefraværsstatistikkNæringMedVarighet.isEmpty()) {
            loggInfoIngenDataTilImport(årstallOgKvartal, "næring med varighet")
            return tomtResultat()
        }
        loggInfoImportStarter(
            sykefraværsstatistikkNæringMedVarighet.size, "næring med varighet", årstallOgKvartal
        )
        val antallSlettet = slettSykefraværsstatistikkNæringMedVarighet(årstallOgKvartal)
        val antallOprettet = batchOpprettSykefraværsstatistikkNæringMedVarighet(
            sykefraværsstatistikkNæringMedVarighet, INSERT_BATCH_STØRRELSE
        )
        return SlettOgOpprettResultat(antallSlettet, antallOprettet)
    }

    fun slettSykefraværsstatistikkNæringMedVarighet(årstallOgKvartal: ÅrstallOgKvartal): Int {
        val namedParameters: SqlParameterSource = MapSqlParameterSource()
            .addValue(SykefraværsstatistikkIntegrasjon.ARSTALL, årstallOgKvartal.årstall)
            .addValue(SykefraværsstatistikkIntegrasjon.KVARTAL, årstallOgKvartal.kvartal)
        return namedParameterJdbcTemplate.update(
            String.format(
                "delete from sykefravar_statistikk_naring_med_varighet where arstall = :%s and kvartal = :%s",
                SykefraværsstatistikkIntegrasjon.ARSTALL, SykefraværsstatistikkIntegrasjon.KVARTAL
            ),
            namedParameters
        )
    }

    fun batchOpprettSykefraværsstatistikkNæringMedVarighet(
        sykefraværsstatistikk: List<Sykefraværsstatistikk>, insertBatchStørrelse: Int
    ): Int {
        val subsets = Lists.partition(sykefraværsstatistikk, insertBatchStørrelse)
        val antallOpprettet = AtomicInteger()
        subsets.forEach { subset: List<Sykefraværsstatistikk?> ->
            val opprettet = opprettSykefraværsstatistikkNæringMedVarighet(subset)
            val opprettetHittilNå = antallOpprettet.addAndGet(opprettet)
            log.info(String.format("Opprettet %d rader", opprettetHittilNå))
        }
        return antallOpprettet.get()
    }

    fun opprettSykefraværsstatistikkNæringMedVarighet(
        sykefraværsstatistikk: List<Sykefraværsstatistikk?>
    ): Int {
        val batch = SqlParameterSourceUtils.createBatch(*sykefraværsstatistikk.toTypedArray())
        val results = namedParameterJdbcTemplate.batchUpdate(
            "insert into sykefravar_statistikk_naring_med_varighet "
                    + "(arstall, kvartal, naring_kode, varighet, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "values "
                    + "(:årstall, :kvartal, :næringkode, :varighet, :antallPersoner, :tapteDagsverk, :muligeDagsverk)",
            batch
        )
        return Arrays.stream(results).sum()
    }

    private fun loggInfoIngenDataTilImport(
        årstallOgKvartal: ÅrstallOgKvartal, beskrivelse: String
    ) {
        log.info(
            String.format(
                "Ingen sykefraværsstatistikk ('%s') til import for årstall '%d' og kvartal '%d'. ",
                beskrivelse, årstallOgKvartal.årstall, årstallOgKvartal.kvartal
            )
        )
    }

    private fun loggInfoImportStarter(
        importSize: Int, beskrivelse: String, årstallOgKvartal: ÅrstallOgKvartal
    ) {
        log.info(
            String.format(
                "Starter import av sykefraværsstatistikk (%s) for årstall '%d' og kvartal '%d'. "
                        + "Skal importere %d rader",
                beskrivelse, årstallOgKvartal.årstall, årstallOgKvartal.kvartal, importSize
            )
        )
    }
}
