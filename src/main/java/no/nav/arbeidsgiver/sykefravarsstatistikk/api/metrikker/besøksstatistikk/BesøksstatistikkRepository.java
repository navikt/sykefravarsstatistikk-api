package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.integrasjoner.altinn.AltinnRolle;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk.sammenligning.Sammenligning;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk.sammenligning.Sykefraværprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class BesøksstatistikkRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final static String ÅRSTALL = "arstall";
    private final static String KVARTAL = "kvartal";
    private final static String SYKEFRAVÆRSPROSENT = "sykefravarsprosent";
    private final static String SYKEFRAVÆRSPROSENT_ANTALL_PERSONER = "sykefravarsprosent_antall_personer";
    private final static String NÆRING_2SIFFER_SYKEFRAVÆRSPROSENT = "naring_2siffer_sykefravarsprosent";
    private final static String BRANSJE_SYKEFRAVÆRSPROSENT = "bransje_sykefravarsprosent";
    private final static String SSB_SEKTOR_SYKEFRAVÆRSPROSENT = "ssb_sektor_sykefravarsprosent";
    private final static String ORGNR = "orgnr";
    private final static String ORGANISASJON_NAVN = "organisasjon_navn";
    private final static String ANTALL_ANSATTE = "antall_ansatte";
    private final static String NÆRING_5SIFFER_KODE = "naring_5siffer_kode";
    private final static String NÆRING_5SIFFER_BESKRIVELSE = "naring_5siffer_beskrivelse";
    private final static String NÆRING_2SIFFER_BESKRIVELSE = "naring_2siffer_beskrivelse";
    private final static String BRANSJE_NAVN = "bransje_navn";
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

    public void lagreBesøkFraStorVirksomhet(SammenligningEvent sammenligningEvent) {

        Sammenligning sammenligning = sammenligningEvent.getSammenligning();
        Underenhet underenhet = sammenligningEvent.getUnderenhet();
        Næringskode5Siffer næring5siffer = sammenligningEvent.getNæring5siffer();
        OverordnetEnhet overordnetEnhet = sammenligningEvent.getOverordnetEnhet();
        Sektor ssbSektor = sammenligningEvent.getSsbSektor();

        Optional<BigDecimal> prosentNæring = Optional.ofNullable(sammenligning.getNæring()).map(Sykefraværprosent::getProsent);
        Optional<BigDecimal> prosentBransje = Optional.ofNullable(sammenligning.getBransje()).map(Sykefraværprosent::getProsent);
        Optional<String> bransjenavn = Optional.ofNullable(sammenligningEvent.getBransje()).map(Bransje::getNavn);

        namedParameterJdbcTemplate.update(
                "insert into besoksstatistikk_virksomhet " +
                        "(arstall, kvartal, sykefravarsprosent, sykefravarsprosent_antall_personer, naring_2siffer_sykefravarsprosent, ssb_sektor_sykefravarsprosent, orgnr, organisasjon_navn, antall_ansatte, naring_5siffer_kode, naring_5siffer_beskrivelse, naring_2siffer_beskrivelse, institusjonell_sektor_kode, institusjonell_sektor_beskrivelse, ssb_sektor_kode, ssb_sektor_beskrivelse, session_id) " +
                        "values (:arstall, :kvartal, :sykefravarsprosent, :sykefravarsprosent_antall_personer, :naring_2siffer_sykefravarsprosent, :ssb_sektor_sykefravarsprosent, :orgnr, :organisasjon_navn, :antall_ansatte, :naring_5siffer_kode, :naring_5siffer_beskrivelse, :naring_2siffer_beskrivelse, :institusjonell_sektor_kode, :institusjonell_sektor_beskrivelse, :ssb_sektor_kode, :ssb_sektor_beskrivelse, :session_id)",
                new MapSqlParameterSource()
                        .addValue(ÅRSTALL, sammenligning.getÅrstall())
                        .addValue(KVARTAL, sammenligning.getKvartal())
                        .addValue(SYKEFRAVÆRSPROSENT, sammenligning.getVirksomhet().getProsent())
                        .addValue(SYKEFRAVÆRSPROSENT_ANTALL_PERSONER, sammenligning.getVirksomhet().getAntallPersoner())
                        .addValue(NÆRING_2SIFFER_SYKEFRAVÆRSPROSENT, prosentNæring.orElse(null))
                        .addValue(BRANSJE_SYKEFRAVÆRSPROSENT, prosentBransje.orElse(null))
                        .addValue(SSB_SEKTOR_SYKEFRAVÆRSPROSENT, sammenligning.getSektor().getProsent())
                        .addValue(ORGNR, underenhet.getOrgnr().getVerdi())
                        .addValue(ORGANISASJON_NAVN, underenhet.getNavn())
                        .addValue(ANTALL_ANSATTE, underenhet.getAntallAnsatte())
                        .addValue(NÆRING_5SIFFER_KODE, næring5siffer.getKode())
                        .addValue(NÆRING_5SIFFER_BESKRIVELSE, næring5siffer.getBeskrivelse())
                        .addValue(NÆRING_2SIFFER_BESKRIVELSE, sammenligningEvent.getNæring2siffer().getNavn())
                        .addValue(BRANSJE_NAVN, bransjenavn.orElse(null))
                        .addValue(INSTITUSJONELL_SEKTOR_KODE, overordnetEnhet.getInstitusjonellSektorkode().getKode())
                        .addValue(INSTITUSJONELL_SEKTOR_BESKRIVELSE, overordnetEnhet.getInstitusjonellSektorkode().getBeskrivelse())
                        .addValue(SSB_SEKTOR_KODE, ssbSektor.getKode())
                        .addValue(SSB_SEKTOR_BESKRIVELSE, ssbSektor.getNavn())
                        .addValue(SESSION_ID, sammenligningEvent.getSessionId())
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

    public void lagreRollerKnyttetTilBesøket(
            int år,
            int uke,
            List<AltinnRolle> altinnRoller
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(
                "insert into besoksstatistikk_unikt_besok (ar, uke) values (:ar, :uke)",
                new MapSqlParameterSource()
                        .addValue("ar", år)
                        .addValue("uke", uke),
                keyHolder,
                new String[]{"id"}
        );

        int uniktBesøkId = keyHolder.getKey().intValue();
        log.info(
                String.format("Lagret unikt besøk '%d' med '%d' roller",
                        uniktBesøkId,
                        altinnRoller.size()
                )
        );
        altinnRoller.stream().forEach(
                rolle ->
                        namedParameterJdbcTemplate.update(
                                "insert into besoksstatistikk_altinn_roller " +
                                        "(unikt_besok_id, rolle_definition_id, rolle_name) " +
                                        "values (:unikt_besok_id, :rolle_definition_id, :rolle_name)",
                                new MapSqlParameterSource()
                                        .addValue("unikt_besok_id", uniktBesøkId)
                                        .addValue("rolle_definition_id", rolle.getDefinitionId())
                                        .addValue("rolle_name", rolle.getName())
                        )
        );
    }
}
