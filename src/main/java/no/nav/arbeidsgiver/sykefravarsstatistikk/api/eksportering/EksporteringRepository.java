package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class EksporteringRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public EksporteringRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate")
                                          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public int opprettEksport(
            List<VirksomhetEksportPerKvartal> virksomhetEksportPerKvartalList
    ) {
        if (virksomhetEksportPerKvartalList == null || virksomhetEksportPerKvartalList.isEmpty()) {
            return 0;
        }
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(virksomhetEksportPerKvartalList.toArray());

        int[] results = namedParameterJdbcTemplate.batchUpdate(
                "insert into eksport_per_kvartal " +
                        "(orgnr, arstall, kvartal) " +
                        "values " +
                        "(:orgnr, :årstall, :kvartal)",
                batch
        );
        return Arrays.stream(results).sum();
    }

    public List<VirksomhetEksportPerKvartal> hentVirksomhetEksportPerKvartal(ÅrstallOgKvartal årstallOgKvartal) {
        SqlParameterSource parametre =
                new MapSqlParameterSource()
                        .addValue("årstall", årstallOgKvartal.getÅrstall())
                        .addValue("kvartal", årstallOgKvartal.getKvartal());

        return namedParameterJdbcTemplate.query(
                "select orgnr, arstall, kvartal, eksportert " +
                        "from eksport_per_kvartal " +
                        "where arstall = :årstall " +
                        "and kvartal = :kvartal",
                parametre,
                (resultSet, rowNum) ->
                        new VirksomhetEksportPerKvartal(
                                new Orgnr(resultSet.getString("orgnr")),
                                new ÅrstallOgKvartal(
                                        resultSet.getInt("arstall"),
                                        resultSet.getInt("kvartal")
                                ),
                                resultSet.getBoolean("eksportert")
                        )
        );
    }

    public void batchOppdaterTilEksportert(
            List<String> virksomheterSomSkalFlaggesSomEksportert,
            ÅrstallOgKvartal årstallOgKvartal
    ) {
        SqlParameterSource parametre =
                new MapSqlParameterSource()
                        .addValue("årstall", årstallOgKvartal.getÅrstall())
                        .addValue("kvartal", årstallOgKvartal.getKvartal())
                        .addValue("orgnrListe", virksomheterSomSkalFlaggesSomEksportert)
                        .addValue("eksportert", true)
                        .addValue("oppdatert", LocalDateTime.now());

        namedParameterJdbcTemplate.update(
                "update eksport_per_kvartal set eksportert = :eksportert, oppdatert = :oppdatert " +
                        "where arstall = :årstall " +
                        "and kvartal = :kvartal " +
                        "and orgnr in (:orgnrListe) ",
                parametre
        );
    }

    @Async
    public void oppdaterTilEksportert(VirksomhetEksportPerKvartal virksomhetTilEksport) {
        SqlParameterSource parametre =
                new MapSqlParameterSource()
                        .addValue("årstall", virksomhetTilEksport.getÅrstall())
                        .addValue("kvartal", virksomhetTilEksport.getKvartal())
                        .addValue("orgnr", virksomhetTilEksport.getOrgnr())
                        .addValue("eksportert", true)
                        .addValue("oppdatert", LocalDateTime.now());

        namedParameterJdbcTemplate.update(
                "update eksport_per_kvartal set eksportert = :eksportert, oppdatert = :oppdatert " +
                        "where arstall = :årstall " +
                        "and kvartal = :kvartal " +
                        "and orgnr = :orgnr ",
                parametre
        );
    }

    public int hentAntallIkkeFerdigEksportert() {
        SqlParameterSource parametre =
                new MapSqlParameterSource()
                        .addValue("eksportert", false);

        return namedParameterJdbcTemplate.queryForObject(
                "select count(*) from eksport_per_kvartal " +
                        "where eksportert = :eksportert ",
                parametre,
                Integer.class
        );
    }

    public int slettEksportertPerKvartal() {
        SqlParameterSource parametre =
                new MapSqlParameterSource();

        return namedParameterJdbcTemplate.update(
                "delete from eksport_per_kvartal",
                parametre);

    }
}
