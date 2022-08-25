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
            return namedParameterJdbcTemplate.query(
                    "select * " +
                            "from dk_p.publiseringstabell" +
                            "where TABELL_NAVN = 'AGG_FAK_SYKEFRAVAR_DIA' " +
                            "and PERIODE_TYPE = 'KVARTAL'" +
                            "order by offentlig_dato desc",
                    new HashMap<>(),
                    (rs, rowNum) -> new PubliseringsdatoDbDto(
                            new ÅrstallOgKvartal(
                                    rs.getInt("arstall"),
                                    rs.getInt("kvartal")
                            ),
                            rs.getString("offentlig_dato"),
                            rs.getString("aktivitet")
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }
}
