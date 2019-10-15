package no.nav.tag.sykefravarsstatistikk.api.provisionering;

import no.nav.tag.sykefravarsstatistikk.api.domene.klassifikasjoner.Sektor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.tag.sykefravarsstatistikk.api.sammenligning.SammenligningRepository.KVARTAL;
import static no.nav.tag.sykefravarsstatistikk.api.sammenligning.SammenligningRepository.ÅRSTALL;

@Component
public class DataverehusRepository {


    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DataverehusRepository(
            @Qualifier("datavarehusJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    public List<Sektor> hentAlleSektorer(int årstall, int kvartal) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue(ÅRSTALL, årstall)
                .addValue(KVARTAL, kvartal);

        return namedParameterJdbcTemplate.queryForList(
                "select * from dt_p.V_DIM_IA_SEKTOR WHERE arstall = :arstall AND kvartal = :kvartal",
                namedParameters,
                Sektor.class);
    }
}
