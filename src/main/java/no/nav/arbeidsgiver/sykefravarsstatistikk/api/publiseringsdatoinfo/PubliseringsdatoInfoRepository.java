package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoinfo;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.publiseringsdatoer.PubliseringsdatoDbDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Component
public class PubliseringsdatoInfoRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public PubliseringsdatoInfoRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;

    }

    public List<PubliseringsdatoDbDto> hentPubliseringsdatoFullInfo() {
        try {
            return namedParameterJdbcTemplate.query("select * from publiseringsdatoer ",
                    new HashMap<>(),
                    (rs, rowNum) -> new PubliseringsdatoDbDto(
                            rs.getInt("rapport_periode"),
                            rs.getDate("offentlig_dato"),
                            rs.getDate("oppdatert_i_dvh"),
                            rs.getString("aktivitet")
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }
}