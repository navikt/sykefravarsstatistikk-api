package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
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

@Slf4j
@Component
public class BesøksstatistikkRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final static String ÅRSTALL = "arstall";
    private final static String KVARTAL = "kvartal";
    private final static String SYKEFRAVÆRSPROSENT = "sykefravarsprosent";
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
    private final static String SESSION_ID = "session_id";

    public BesøksstatistikkRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public boolean sessionHarBlittRegistrert(String sessionId, Orgnr orgnr) {
        if (kombinasjonEksistererITabellForStoreVirksomheter(sessionId, orgnr)) {
            return true;
        }
        return sessionIdEksisterITabellForSmåVirksomheter(sessionId);
    }

    private boolean kombinasjonEksistererITabellForStoreVirksomheter(String sessionId, Orgnr orgnr) {
        return namedParameterJdbcTemplate.queryForObject(
                "select count(*) from besoksstatistikk_virksomhet where session_id=(:session_id) and orgnr=(:orgnr)",
                new MapSqlParameterSource()
                        .addValue(SESSION_ID, sessionId)
                        .addValue(ORGNR, orgnr.getVerdi()),
                Integer.class
        ) > 0;
    }

    private boolean sessionIdEksisterITabellForSmåVirksomheter(String sessionId) {
        return namedParameterJdbcTemplate.queryForObject(
                "select count(*) from besoksstatistikk_smaa_virksomheter where session_id=(:session_id)",
                new MapSqlParameterSource()
                        .addValue(SESSION_ID, sessionId),
                Integer.class
        ) > 0;
    }

    public void lagreBesøkFraStorVirksomhet(
            Enhet enhet,
            Underenhet underenhet,
            Sektor ssbSektor,
            Næringskode5Siffer næring5siffer,
            Næring næring2siffer,
            Sammenligning sammenligning,
            String sessionId
    ) {

        namedParameterJdbcTemplate.update(
                "insert into besoksstatistikk_virksomhet " +
                        "(arstall, kvartal, sykefravarsprosent, sykefravarsprosent_antall_personer, naring_2siffer_sykefravarsprosent, ssb_sektor_sykefravarsprosent, orgnr, organisasjon_navn, antall_ansatte, naring_5siffer_kode, naring_5siffer_beskrivelse, naring_2siffer_beskrivelse, institusjonell_sektor_kode, institusjonell_sektor_beskrivelse, ssb_sektor_kode, ssb_sektor_beskrivelse, session_id) " +
                        "values (:arstall, :kvartal, :sykefravarsprosent, :sykefravarsprosent_antall_personer, :naring_2siffer_sykefravarsprosent, :ssb_sektor_sykefravarsprosent, :orgnr, :organisasjon_navn, :antall_ansatte, :naring_5siffer_kode, :naring_5siffer_beskrivelse, :naring_2siffer_beskrivelse, :institusjonell_sektor_kode, :institusjonell_sektor_beskrivelse, :ssb_sektor_kode, :ssb_sektor_beskrivelse, :session_id)",
                new MapSqlParameterSource()
                        .addValue(ÅRSTALL, sammenligning.getÅrstall())
                        .addValue(KVARTAL, sammenligning.getKvartal())
                        .addValue(SYKEFRAVÆRSPROSENT, sammenligning.getVirksomhet().getProsent())
                        .addValue(SYKEFRAVÆRSPROSENT_ANTALL_PERSONER, sammenligning.getVirksomhet().getAntallPersoner())
                        .addValue(NÆRING_2SIFFER_SYKEFRAVÆRSPROSENT, sammenligning.getNæring().getProsent())
                        .addValue(SSB_SEKTOR_SYKEFRAVÆRSPROSENT, sammenligning.getSektor().getProsent())
                        .addValue(ORGNR, underenhet.getOrgnr().getVerdi())
                        .addValue(ORGANISASJON_NAVN, underenhet.getNavn())
                        .addValue(ANTALL_ANSATTE, underenhet.getAntallAnsatte())
                        .addValue(NÆRING_5SIFFER_KODE, næring5siffer.getKode())
                        .addValue(NÆRING_5SIFFER_BESKRIVELSE, næring5siffer.getBeskrivelse())
                        .addValue(NÆRING_2SIFFER_BESKRIVELSE, næring2siffer.getNavn())
                        .addValue(INSTITUSJONELL_SEKTOR_KODE, enhet.getInstitusjonellSektorkode().getKode())
                        .addValue(INSTITUSJONELL_SEKTOR_BESKRIVELSE, enhet.getInstitusjonellSektorkode().getBeskrivelse())
                        .addValue(SSB_SEKTOR_KODE, ssbSektor.getKode())
                        .addValue(SSB_SEKTOR_BESKRIVELSE, ssbSektor.getNavn())
                        .addValue(SESSION_ID, sessionId)
        );
    }

    public void lagreBesøkFraLitenVirksomhet(
            String sessionId
    ) {
        namedParameterJdbcTemplate.update(
                "insert into besoksstatistikk_smaa_virksomheter (session_id) values (:session_id)",
                new MapSqlParameterSource().addValue(SESSION_ID, sessionId)
        );
    }

}
