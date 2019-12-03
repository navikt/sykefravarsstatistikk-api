package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
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

    private final static String ÅRSTALL = "arstall";
    private final static String KVARTAL = "kvartal";
    private final static String SYKEFRAVÆRSPROSENT = "sykefravarsprosent";
    private final static String SYKEFRAVÆRSPROSENT_ER_MASKERT = "sykefravarsprosent_er_maskert";
    private final static String SYKEFRAVÆRSPROSENT_ANTALL_PERSONER = "sykefravarsprosent_antall_personer";
    private final static String NÆRING_2SIFFER_SYKEFRAVÆRSPROSENT = "naring_2siffer_sykefravarsprosent";
    private final static String SSB_SEKTOR_SYKEFRAVÆRSPROSENT = "ssb_sektor_sykefravarsprosent";
    private final static String ORGNR = "orgnr";
    private final static String ORGANISASJON_NAVN = "organisasjon_navn";
    private final static String ANTALL_ANSATTE = "antall_ansatte";
    private final static String NÆRING_5SIFFER_KODE = "naring_5siffer_kode";
    private final static String NÆRING_5SIFFER_BESKRIVELSE = "naring_5siffer_beskrivelse";
    private final static String NÆRING_2SIFFER_BESKRIVELSE = "naring_2siffer_beskrivelse";
    private final static String INSTITUSJONELL_SEKTOR_KODE = "institusjonell_sektor_kode";
    private final static String INSTITUSJONELL_SEKTOR_BESKRIVELSE = "institusjonell_sektor_beskrivelse";
    private final static String SSB_SEKTOR_KODE = "ssb_sektor_kode";
    private final static String SSB_SEKTOR_BESKRIVELSE = "ssb_sektor_beskrivelse";
    private final static String COOKIE = "cookie";

    public BesøksstatistikkRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public void loggBesøk(
            Underenhet underenhet,
            Enhet enhet,
            Sektor ssbSektor,
            Næringskode5Siffer næring5siffer,
            Næring næring2siffer,
            Sammenligning sammenligning
    ) {


        if (kanLagreBesøksdata(underenhet, sammenligning)) {
            namedParameterJdbcTemplate.update(
                    "insert into besoksstatistikk_virksomhet " +
                            "(arstall, kvartal, sykefravarsprosent, sykefravarsprosent_antall_personer, naring_2siffer_sykefravarsprosent, ssb_sektor_sykefravarsprosent, orgnr, organisasjon_navn, antall_ansatte, naring_5siffer_kode, naring_5siffer_beskrivelse, naring_2siffer_beskrivelse, institusjonell_sektor_kode, institusjonell_sektor_beskrivelse, ssb_sektor_kode, ssb_sektor_beskrivelse, cookie) " +
                            "values (:arstall, :kvartal, :sykefravarsprosent, :sykefravarsprosent_antall_personer, :naring_2siffer_sykefravarsprosent, :ssb_sektor_sykefravarsprosent, :orgnr, :organisasjon_navn, :antall_ansatte, :naring_5siffer_kode, :naring_5siffer_beskrivelse, :naring_2siffer_beskrivelse, :institusjonell_sektor_kode, :institusjonell_sektor_beskrivelse, :ssb_sektor_kode, :ssb_sektor_beskrivelse, :cookie)",
                    new MapSqlParameterSource()
                            .addValue(ÅRSTALL, sammenligning.getÅrstall())
                            .addValue(KVARTAL, sammenligning.getKvartal())
                            .addValue(SYKEFRAVÆRSPROSENT, sammenligning.getVirksomhet().getProsent())
                            .addValue(SYKEFRAVÆRSPROSENT_ANTALL_PERSONER, sammenligning.getVirksomhet().getAntallPersoner())
                            .addValue(NÆRING_2SIFFER_SYKEFRAVÆRSPROSENT, sammenligning.getNæring().getProsent())
                            .addValue(SSB_SEKTOR_SYKEFRAVÆRSPROSENT, sammenligning.getSektor().getProsent())
                            .addValue(ORGNR, enhet.getOrgnr().getVerdi())
                            .addValue(ORGANISASJON_NAVN, enhet.getNavn())
                            .addValue(ANTALL_ANSATTE, enhet.getAntallAnsatte())
                            .addValue(NÆRING_5SIFFER_KODE, næring5siffer.getKode())
                            .addValue(NÆRING_5SIFFER_BESKRIVELSE, næring5siffer.getBeskrivelse())
                            .addValue(NÆRING_2SIFFER_BESKRIVELSE, næring2siffer.getNavn())
                            .addValue(INSTITUSJONELL_SEKTOR_KODE, enhet.getInstitusjonellSektorkode().getKode())
                            .addValue(INSTITUSJONELL_SEKTOR_BESKRIVELSE, enhet.getInstitusjonellSektorkode().getBeskrivelse())
                            .addValue(SSB_SEKTOR_KODE, ssbSektor.getKode())
                            .addValue(SSB_SEKTOR_BESKRIVELSE, ssbSektor.getNavn())
                            .addValue(COOKIE, sammenligning.getÅrstall())
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

    private boolean kanLagreBesøksdata(Underenhet underenhet, Sammenligning sammenligning) {
        return !sammenligning.getVirksomhet().isErMaskert()
                && underenhet.getAntallAnsatte() >= MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
    }
}
