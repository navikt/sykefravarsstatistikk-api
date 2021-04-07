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
public class EksportRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public EksportRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate")
                                                NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public int opprettEksport(
            List<VirksomhetMetadata> virksomhetMetadata
    ) {
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(virksomhetMetadata.toArray());

        int[] results = namedParameterJdbcTemplate.batchUpdate(
                "insert into eksport_per_kvartal " +
                        "(orgnr, arstall, kvartal) " +
                        "values " +
                        "(:orgnr, :årstall, :kvartal)",
                batch
        );
        return Arrays.stream(results).sum();
    }

    // hent()
    // oppdater()
}
