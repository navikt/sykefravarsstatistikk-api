package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class VirksomhetMetadataRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public VirksomhetMetadataRepository(
      @Qualifier("sykefravarsstatistikkJdbcTemplate")
          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  public int opprettVirksomhetMetadata(List<VirksomhetMetadata> virksomhetMetadata) {
    SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(virksomhetMetadata.toArray());

    int[] results =
        namedParameterJdbcTemplate.batchUpdate(
            "insert into virksomhet_metadata "
                + "(orgnr, navn, rectype, sektor, naring_kode, arstall, kvartal) "
                + "values "
                + "(:orgnr, :navn, :rectype, :sektor, :næring, :årstall, :kvartal)",
            batch);
    return Arrays.stream(results).sum();
  }

  public int opprettVirksomhetMetadataNæringskode5siffer(
      List<VirksomhetMetadataNæringskode5siffer> virksomhetMetadataNæringskode5siffer) {
    SqlParameterSource[] batch =
        SqlParameterSourceUtils.createBatch(virksomhetMetadataNæringskode5siffer.toArray());

    int[] results =
        namedParameterJdbcTemplate.batchUpdate(
            "insert into virksomhet_metadata_naring_kode_5siffer "
                + "(orgnr, naring_kode, naring_kode_5siffer, arstall, kvartal) "
                + "values "
                + "(:orgnr, :næring, :næringskode5siffer, :årstall, :kvartal)",
            batch);
    return Arrays.stream(results).sum();
  }

  public List<VirksomhetMetadata> hentVirksomhetMetadata(ÅrstallOgKvartal årstallOgKvartal) {
    MapSqlParameterSource paramSource =
        new MapSqlParameterSource()
            .addValue("årstall", årstallOgKvartal.getÅrstall())
            .addValue("kvartal", årstallOgKvartal.getKvartal());

    List<VirksomhetMetadata> virksomhetMetadata =
        namedParameterJdbcTemplate.query(
            "select orgnr, navn, rectype, sektor, naring_kode, arstall, kvartal "
                + "from virksomhet_metadata "
                + "where arstall = :årstall "
                + "and kvartal = :kvartal ",
            paramSource,
            ((resultSet, i) ->
                new VirksomhetMetadata(
                    new Orgnr(resultSet.getString("orgnr")),
                    resultSet.getString("navn"),
                    resultSet.getString("rectype"),
                    resultSet.getString("sektor"),
                    resultSet.getString("naring_kode"),
                    new ÅrstallOgKvartal(
                        resultSet.getInt("arstall"), resultSet.getInt("kvartal")))));

    List<Pair<Orgnr, NæringOgNæringskode5siffer>> næringOgNæringskode5siffer =
        namedParameterJdbcTemplate.query(
            "SELECT orgnr, naring_kode, naring_kode_5siffer "
                + "FROM virksomhet_metadata_naring_kode_5siffer "
                + "WHERE arstall = :årstall AND kvartal = :kvartal",
            paramSource,
            ((resultSet, i) ->
                Pair.of(
                    new Orgnr(resultSet.getString("orgnr")),
                    new NæringOgNæringskode5siffer(
                        resultSet.getString("naring_kode"),
                        resultSet.getString("naring_kode_5siffer")))));

    return assemble(virksomhetMetadata, næringOgNæringskode5siffer);
  }

  protected List<VirksomhetMetadata> assemble(
      List<VirksomhetMetadata> virksomhetMetadata,
      List<Pair<Orgnr, NæringOgNæringskode5siffer>> næringOgNæringskode5siffer) {

    Map<Orgnr, List<NæringOgNæringskode5siffer>> map =
        næringOgNæringskode5siffer.stream()
            .collect(
                Collectors.groupingBy(
                    Pair::getFirst, Collectors.mapping(Pair::getSecond, Collectors.toList())));

    virksomhetMetadata.forEach(
        vm -> vm.leggTilNæringOgNæringskode5siffer(map.get(new Orgnr(vm.getOrgnr()))));

    return virksomhetMetadata;
  }

  public int slettVirksomhetMetadata() {
    MapSqlParameterSource parametre = new MapSqlParameterSource();

    return namedParameterJdbcTemplate.update("delete from virksomhet_metadata", parametre);
  }

  public int slettNæringOgNæringskode5siffer() {
    MapSqlParameterSource parametre = new MapSqlParameterSource();

    return namedParameterJdbcTemplate.update(
        "delete from virksomhet_metadata_naring_kode_5siffer", parametre);
  }
}
