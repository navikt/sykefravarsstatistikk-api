package no.nav.tag.sykefravarsstatistikk.api.provisionering;

import no.nav.tag.sykefravarsstatistikk.api.domene.klassifikasjoner.Sektor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProvisioneringRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public ProvisioneringRepository(
            @Qualifier("applikasjonJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    public void oppdaterSektorer(List<Sektor> sektorer) {
        sektorer.stream().forEach( s -> leggTilSektor(s) );

    }

    private int leggTilSektor(Sektor sektor) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("kode", sektor.getKode())
                .addValue("navn", sektor.getNavn());

        return namedParameterJdbcTemplate.update(
                "insert into SEKTOR (kode, navn)  values (:kode, :navn)",
                namedParameters);
    }

}
