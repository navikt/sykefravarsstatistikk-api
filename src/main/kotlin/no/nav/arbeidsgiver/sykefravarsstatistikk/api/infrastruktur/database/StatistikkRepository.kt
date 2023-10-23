package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import com.google.common.collect.Lists
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.SlettOgOpprettResultat.Companion.tomtResultat
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Component
class StatistikkRepository(
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
) {
    val INSERT_BATCH_STØRRELSE = 10000

    private val log = LoggerFactory.getLogger(this::class.java)

    fun hentSisteÅrstallOgKvartalForSykefraværsstatistikk(type: Statistikkilde): ÅrstallOgKvartal {
        val alleÅrstallOgKvartal = hentAlleÅrstallOgKvartalForSykefraværsstatistikk(type)
        log.info("Henter statistikk for type {}", type.tabell)
        return try {
            alleÅrstallOgKvartal[0]
        } catch (e: IndexOutOfBoundsException) {
            log.error("Error ved uthenting av statistikk for type {}", type.tabell)
            throw e
        }
    }

    fun hentAlleÅrstallOgKvartalForSykefraværsstatistikk(
        type: Statistikkilde
    ): List<ÅrstallOgKvartal> {
        return namedParameterJdbcTemplate.query(
            String.format(
                "select distinct arstall, kvartal "
                        + "from %s "
                        + "order by arstall desc, kvartal desc",
                type.tabell
            ),
            MapSqlParameterSource()
        ) { resultSet: ResultSet, _: Int ->
            ÅrstallOgKvartal(
                resultSet.getInt("arstall"),
                resultSet.getInt("kvartal")
            )
        }
    }

    fun importSykefraværsstatistikkSektor(
        sykefraværsstatistikkSektor: List<SykefraværsstatistikkSektor>,
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        val sykefraværsstatistikkSektorUtils = SykefraværsstatistikkSektorUtils(
            namedParameterJdbcTemplate
        )
        return importStatistikk(
            "sektor", sykefraværsstatistikkSektor, årstallOgKvartal, sykefraværsstatistikkSektorUtils
        )
    }

    fun importSykefraværsstatistikkNæring(
        sykefraværsstatistikkForNæring: List<SykefraværsstatistikkForNæring>,
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        val sykefraværsstatistikkNæringUtils = SykefraværsstatistikkNæringUtils(
            namedParameterJdbcTemplate
        )
        return importStatistikk(
            "næring", sykefraværsstatistikkForNæring, årstallOgKvartal, sykefraværsstatistikkNæringUtils
        )
    }

    fun importSykefraværsstatistikkNæring5siffer(
        sykefraværsstatistikkForNæring: List<SykefraværsstatistikkForNæring>,
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        val sykefraværsstatistikkNæring5sifferUtils = SykefraværsstatistikkNæring5sifferUtils(
            namedParameterJdbcTemplate
        )
        return importStatistikk(
            "næring5siffer",
            sykefraværsstatistikkForNæring,
            årstallOgKvartal,
            sykefraværsstatistikkNæring5sifferUtils
        )
    }

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

    fun importSykefraværsstatistikkVirksomhetMedGradering(
        sykefraværsstatistikkVirksomhetMedGradering: List<SykefraværsstatistikkVirksomhetMedGradering>,
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        if (sykefraværsstatistikkVirksomhetMedGradering.isEmpty()) {
            loggInfoIngenDataTilImport(årstallOgKvartal, "virksomhet gradert sykemelding")
            return tomtResultat()
        }
        loggInfoImportStarter(
            sykefraværsstatistikkVirksomhetMedGradering.size,
            "virksomhet gradert sykemelding",
            årstallOgKvartal
        )
        val antallSlettet = slettSykefraværsstatistikkVirksomhetMedGradering(årstallOgKvartal)
        val antallOprettet = batchOpprettSykefraværsstatistikkVirksomhetMedGradering(
            sykefraværsstatistikkVirksomhetMedGradering, INSERT_BATCH_STØRRELSE
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

    fun slettSykefraværsstatistikkVirksomhetMedGradering(årstallOgKvartal: ÅrstallOgKvartal): Int {
        val namedParameters: SqlParameterSource = MapSqlParameterSource()
            .addValue(SykefraværsstatistikkIntegrasjon.ARSTALL, årstallOgKvartal.årstall)
            .addValue(SykefraværsstatistikkIntegrasjon.KVARTAL, årstallOgKvartal.kvartal)
        return namedParameterJdbcTemplate.update(
            String.format(
                "delete from sykefravar_statistikk_virksomhet_med_gradering where arstall = :%s and kvartal = :%s",
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

    fun batchOpprettSykefraværsstatistikkVirksomhetMedGradering(
        sykefraværsstatistikk: List<Sykefraværsstatistikk>, insertBatchStørrelse: Int
    ): Int {
        val subsets = Lists.partition(sykefraværsstatistikk, insertBatchStørrelse)
        val antallOpprettet = AtomicInteger()
        subsets.forEach { subset: List<Sykefraværsstatistikk?> ->
            val opprettet = opprettSykefraværsstatistikkVirksomhetMedGradering(subset)
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

    private fun opprettSykefraværsstatistikkVirksomhetMedGradering(
        sykefraværsstatistikk: List<Sykefraværsstatistikk?>
    ): Int {
        val batch = SqlParameterSourceUtils.createBatch(*sykefraværsstatistikk.toTypedArray())
        val results = namedParameterJdbcTemplate.batchUpdate(
            "insert into sykefravar_statistikk_virksomhet_med_gradering "
                    + "(arstall, kvartal, orgnr, naring, naring_kode, rectype, "
                    + "antall_graderte_sykemeldinger, tapte_dagsverk_gradert_sykemelding, "
                    + "antall_sykemeldinger, "
                    + "antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "values "
                    + "(:årstall, :kvartal, :orgnr, :næring, :næringkode, :rectype, "
                    + ":antallGraderteSykemeldinger, :tapteDagsverkGradertSykemelding, "
                    + ":antallSykemeldinger, "
                    + ":antallPersoner, :tapteDagsverk, :muligeDagsverk)",
            batch
        )
        return Arrays.stream(results).sum()
    }

    fun importStatistikk(
        statistikktype: String,
        sykefraværsstatistikk: List<Sykefraværsstatistikk>,
        årstallOgKvartal: ÅrstallOgKvartal,
        sykefraværsstatistikkIntegrasjonUtils: SykefraværsstatistikkIntegrasjonUtils
    ): SlettOgOpprettResultat {
        if (sykefraværsstatistikk.isEmpty()) {
            log.info(
                String.format(
                    "Ingen sykefraværsstatistikk (%s) til import for årstall '%d' og kvartal '%d'. ",
                    statistikktype, årstallOgKvartal.årstall, årstallOgKvartal.kvartal
                )
            )
            return tomtResultat()
        }
        log.info(
            String.format(
                "Starter import av sykefraværsstatistikk (%s) for årstall '%d' og kvartal '%d'. "
                        + "Skal importere %d rader",
                statistikktype,
                årstallOgKvartal.årstall,
                årstallOgKvartal.kvartal,
                sykefraværsstatistikk.size
            )
        )
        val antallSlettet = slett(årstallOgKvartal, sykefraværsstatistikkIntegrasjonUtils.getDeleteFunction())
        val antallOprettet = batchOpprett(
            sykefraværsstatistikk, sykefraværsstatistikkIntegrasjonUtils, INSERT_BATCH_STØRRELSE
        )
        return SlettOgOpprettResultat(antallSlettet, antallOprettet)
    }

    fun batchOpprett(
        sykefraværsstatistikk: List<Sykefraværsstatistikk>,
        utils: SykefraværsstatistikkIntegrasjonUtils,
        insertBatchStørrelse: Int
    ): Int {
        val subsets = sykefraværsstatistikk.chunked(insertBatchStørrelse)
        val antallOpprettet = AtomicInteger()
        subsets.forEach { subset: List<Sykefraværsstatistikk> ->
            val opprettet = utils.getBatchCreateFunction(subset).apply()
            val opprettetHittilNå = antallOpprettet.addAndGet(opprettet)
            log.info(String.format("Opprettet %d rader", opprettetHittilNå))
        }
        return antallOpprettet.get()
    }


    private fun slett(
        årstallOgKvartal: ÅrstallOgKvartal, deleteFunction: DeleteSykefraværsstatistikkFunction
    ): Int {
        return deleteFunction.apply(årstallOgKvartal)
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
