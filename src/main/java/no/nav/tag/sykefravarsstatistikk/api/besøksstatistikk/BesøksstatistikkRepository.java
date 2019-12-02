package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import static no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent.MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;

@Slf4j
@Component
public class BesøksstatistikkRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SYKEFRAVÆRSPROSENT = "sykefravarsprosent";
    private static final String ANTALL_ANSATTE = "antall_ansatte";
    private static final String ORGNR = "orgnr";
    private static final String NÆRING_KODE = "naring_kode";
    private static final String SEKTOR_KODE = "sektor_kode";
    private static final String ÅRSTALL = "arstall";
    private static final String KVARTAL = "kvartal";
    private static final String COOKIE = "cookie";

    private static final String ANTALL_SMÅ_VIRKSOMHETER = "antall_smaa_virksomheter";

    public BesøksstatistikkRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    public void loggBesøk(
            Underenhet underenhet,
            Enhet enhet,
            Sektor ssbSektor,
            Næring næring,
            Sammenligning sammenligning
    ) {
        if (underenhet.getAntallAnsatte() >= MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER) {
            namedParameterJdbcTemplate.update(
                    "insert into besoksstatistikk_virksomhet " +
                            "(sykefravarsprosent, antall_ansatte, orgnr, naring_kode, sektor_kode, arstall, kvartal, cookie) " +
                            "values (:sykefravarsprosent, :antall_ansatte, :orgnr, :naring_kode, :sektor_kode, :arstall, :kvartal, :cookie)",
                    new MapSqlParameterSource()
                            .addValue(SYKEFRAVÆRSPROSENT, sammenligning.getVirksomhet().getProsent())
                            .addValue(ANTALL_ANSATTE, underenhet.getAntallAnsatte())
                            .addValue(ORGNR, underenhet.getOrgnr().getVerdi())
                            .addValue(NÆRING_KODE, underenhet.getNæringskode().getKode())
                            .addValue(SEKTOR_KODE, enhet.getInstitusjonellSektorkode().getKode())
                            .addValue(ÅRSTALL, sammenligning.getÅrstall())
                            .addValue(KVARTAL, sammenligning.getKvartal())
                            .addValue(COOKIE, "cookie")
            );
        } else {
            namedParameterJdbcTemplate.update(
                    "insert into besoksstatistikk_smaa_virksomheter (antall_smaa_virksomheter, cookie) values (1, :cookie)",
                    new MapSqlParameterSource()
                            .addValue(COOKIE, "cookie")
            );
        }

        log.info("done");
    }
}
