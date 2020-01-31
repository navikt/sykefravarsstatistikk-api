package no.nav.tag.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static no.nav.tag.sykefravarsstatistikk.api.sammenligning.SammenligningRepository.ORGNR;

@Component
public class TapteDagsverkForKostnadsberegningRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public TapteDagsverkForKostnadsberegningRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<TapteDagsverk> hentTapteDagsverkFor4Kvartaler(List<ÅrstallOgKvartal> årstallOgKvartalListe, Orgnr orgnr) {
        if (årstallOgKvartalListe.size() != 4) {
            throw new IllegalArgumentException("Kan bare sende inn 4 kvartaler");
        }

        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue(ORGNR, orgnr.getVerdi());

        for (int i = 0; i < årstallOgKvartalListe.size(); i++) {
            namedParameters.addValue("kvartal" + (i + 1), årstallOgKvartalListe.get(i).getKvartal());
            namedParameters.addValue("arstall" + (i + 1), årstallOgKvartalListe.get(i).getÅrstall());
        }

        return namedParameterJdbcTemplate.query(
                "SELECT tapte_dagsverk, kvartal, arstall FROM SYKEFRAVAR_STATISTIKK_VIRKSOMHET where orgnr=:orgnr and " +
                        "((ARSTALL = :arstall1 and kvartal = :kvartal1) or " +
                        "(ARSTALL = :arstall2 and kvartal = :kvartal2) or " +
                        "(ARSTALL = :arstall3 and kvartal = :kvartal3) or " +
                        "(ARSTALL = :arstall4 and kvartal = :kvartal4))",
                namedParameters,
                (rs, rowNum) -> mapTilTapteDagsverk(rs)
        );
    }

    private TapteDagsverk mapTilTapteDagsverk(ResultSet rs) throws SQLException {
        return new TapteDagsverk(
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getInt("arstall"),
                rs.getInt("kvartal")
        );
    }

}
