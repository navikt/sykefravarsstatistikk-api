package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class VirksomhetMetadataRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public VirksomhetMetadataRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate")
                                              NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public int opprettVirksomhetMetadata(
            List<VirksomhetMetadata> virksomhetMetadata
    ) {
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(virksomhetMetadata.toArray());

        int[] results = namedParameterJdbcTemplate.batchUpdate(
                "insert into virksomhet_metadata_til_eksportering " +
                        "(orgnr, arstall, kvartal, sektor, naring_kode, naring_kode_5siffer) " +
                        "values " +
                        "(:orgnr, :årstall, :kvartal, :sektor, :næring, :næringskode5Siffer)",
                batch
        );
        return Arrays.stream(results).sum();
    }
}
