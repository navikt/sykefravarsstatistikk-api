package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering;

import no.nav.tag.sykefravarsstatistikk.api.common.OpprettEllerOppdaterResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppering;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Virksomhetsklassifikasjon;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils.NæringIntegrasjonUtils;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils.SektorIntegrasjonUtils;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils.VirksomhetsklassifikasjonIntegrasjonUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class VirksomhetsklassifikasjonerSynkroniseringRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public static final String KODE_5SIFFER = "kode_5siffer";
    public static final String BESKRIVELSE_5SIFFER = "beskrivelse_5siffer";
    public static final String KODE_4SIFFER = "kode_4siffer";
    public static final String BESKRIVELSE_4SIFFER = "beskrivelse_4siffer";
    public static final String KODE_3SIFFER = "kode_3siffer";
    public static final String BESKRIVELSE_3SIFFER = "beskrivelse_3siffer";
    public static final String KODE_2SIFFER = "kode_2siffer";
    public static final String BESKRIVELSE_2SIFFER = "beskrivelse_2siffer";
    public static final String KODE_HOVEDOMRADE = "kode_hovedomrade";
    public static final String BESKRIVELSE_HOVEDOMRADE = "beskrivelse_hovedomrade";

    public VirksomhetsklassifikasjonerSynkroniseringRepository(
            @Qualifier("sykefravarsstatistikkJdbcTemplate")
                    NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public OpprettEllerOppdaterResultat opprettEllerOppdaterSektorer(List<Sektor> sektorerIDatavarehus) {
        return opprettEllerOppdater(sektorerIDatavarehus, new SektorIntegrasjonUtils(namedParameterJdbcTemplate));
    }

    public OpprettEllerOppdaterResultat opprettEllerOppdaterNæringer(List<Næring> næringer) {
        return opprettEllerOppdater(næringer, new NæringIntegrasjonUtils(namedParameterJdbcTemplate));
    }

    public OpprettEllerOppdaterResultat opprettEllerOppdaterNæringsgrupperinger(List<Næringsgruppering> næringsgrupperinger) {
        OpprettEllerOppdaterResultat sluttResultat = new OpprettEllerOppdaterResultat();
        næringsgrupperinger.forEach(næringsgruppering -> sluttResultat.add(opprettEllerOppdater(næringsgruppering)));
        return sluttResultat;
    }

    private OpprettEllerOppdaterResultat opprettEllerOppdater(
            List<? extends Virksomhetsklassifikasjon> virksomhetsklassifikasjoner,
            VirksomhetsklassifikasjonIntegrasjonUtils integrasjonUtils) {

        final OpprettEllerOppdaterResultat sluttResultat = new OpprettEllerOppdaterResultat();

        virksomhetsklassifikasjoner.forEach(
                virksomhetsklassifikasjon -> {
                    OpprettEllerOppdaterResultat result =
                            opprettEllerOppdater(
                                    virksomhetsklassifikasjon, integrasjonUtils);
                    sluttResultat.add(result);
                });

        return sluttResultat;
    }

    private OpprettEllerOppdaterResultat opprettEllerOppdater(
            Virksomhetsklassifikasjon virksomhetsklassifikasjon,
            VirksomhetsklassifikasjonIntegrasjonUtils integrasjonUtils) {
        final OpprettEllerOppdaterResultat resultat = new OpprettEllerOppdaterResultat();

        integrasjonUtils.getFetchFunction()
                .apply(virksomhetsklassifikasjon)
                .ifPresentOrElse(
                        eksisterende -> {
                            if (!eksisterende.equals(virksomhetsklassifikasjon)) {
                                integrasjonUtils.getUpdateFunction().apply(eksisterende, virksomhetsklassifikasjon);
                                resultat.setAntallRadOppdatert(1);
                            }
                        },
                        () -> {
                            integrasjonUtils.getCreateFunction().apply(virksomhetsklassifikasjon);
                            resultat.setAntallRadOpprettet(1);
                        });

        return resultat;
    }

    private OpprettEllerOppdaterResultat opprettEllerOppdater(
            Næringsgruppering næringsgruppering
    ) {
        OpprettEllerOppdaterResultat resultat = new OpprettEllerOppdaterResultat();

        Optional<Næringsgruppering> næringsgrupperingFraApplikasjonensDatabase = fetchNæringsgruppering(næringsgruppering);

        if (næringsgrupperingFraApplikasjonensDatabase.isPresent()) {
            oppdaterNæringsgruppering(næringsgruppering);
            resultat.setAntallRadOppdatert(1);
        } else {
            opprettNæringsgruppering(næringsgruppering);
            resultat.setAntallRadOpprettet(1);
        }
        return resultat;
    }

    private Optional<Næringsgruppering> fetchNæringsgruppering(Næringsgruppering næringsgruppering) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE_5SIFFER, næringsgruppering.getKode5siffer());

        try {
            Næringsgruppering hentetNæringsgruppering =
                    namedParameterJdbcTemplate.queryForObject(
                            "select kode_5siffer, beskrivelse_5siffer, kode_4siffer, beskrivelse_4siffer, kode_3siffer, beskrivelse_3siffer, kode_2siffer, beskrivelse_2siffer, kode_hovedomrade, beskrivelse_hovedomrade from naringsgruppering where kode_5siffer = :kode_5siffer",
                            namedParameters,
                            (resultSet, rowNum) -> mapTilNæringsgruppering(resultSet));
            return Optional.of(hentetNæringsgruppering);
        } catch (EmptyResultDataAccessException erdae) {
            return Optional.empty();
        }
    }

    private static Næringsgruppering mapTilNæringsgruppering(ResultSet rs) throws SQLException {
        return new Næringsgruppering(
                rs.getString(KODE_5SIFFER),
                rs.getString(BESKRIVELSE_5SIFFER),
                rs.getString(KODE_4SIFFER),
                rs.getString(BESKRIVELSE_4SIFFER),
                rs.getString(KODE_3SIFFER),
                rs.getString(BESKRIVELSE_3SIFFER),
                rs.getString(KODE_2SIFFER),
                rs.getString(BESKRIVELSE_2SIFFER),
                rs.getString(KODE_HOVEDOMRADE),
                rs.getString(BESKRIVELSE_HOVEDOMRADE)
        );
    }

    private int oppdaterNæringsgruppering(Næringsgruppering næringsgruppering) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(BESKRIVELSE_5SIFFER, næringsgruppering.getBeskrivelse5siffer())
                        .addValue(BESKRIVELSE_4SIFFER, næringsgruppering.getBeskrivelse4siffer())
                        .addValue(BESKRIVELSE_3SIFFER, næringsgruppering.getBeskrivelse3siffer())
                        .addValue(BESKRIVELSE_2SIFFER, næringsgruppering.getBeskrivelse2siffer())
                        .addValue(BESKRIVELSE_HOVEDOMRADE, næringsgruppering.getBeskrivelseHovedområde())
                        .addValue(KODE_5SIFFER, næringsgruppering.getKode5siffer());

        return namedParameterJdbcTemplate.update(
                "update naringsgruppering set beskrivelse_5siffer = :beskrivelse_5siffer, beskrivelse_4siffer = :beskrivelse_4siffer, beskrivelse_3siffer = :beskrivelse_3siffer, beskrivelse_2siffer = :beskrivelse_2siffer, beskrivelse_hovedomrade = :beskrivelse_hovedomrade " +
                        "where kode_5siffer = :kode_5siffer",
                namedParameters
        );
    }

    private int opprettNæringsgruppering(Næringsgruppering næringsgruppering) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource()
                        .addValue(KODE_5SIFFER, næringsgruppering.getKode5siffer())
                        .addValue(BESKRIVELSE_5SIFFER, næringsgruppering.getBeskrivelse5siffer())
                        .addValue(KODE_4SIFFER, næringsgruppering.getKode4siffer())
                        .addValue(BESKRIVELSE_4SIFFER, næringsgruppering.getBeskrivelse4siffer())
                        .addValue(KODE_3SIFFER, næringsgruppering.getKode3siffer())
                        .addValue(BESKRIVELSE_3SIFFER, næringsgruppering.getBeskrivelse3siffer())
                        .addValue(KODE_2SIFFER, næringsgruppering.getKode2siffer())
                        .addValue(BESKRIVELSE_2SIFFER, næringsgruppering.getBeskrivelse2siffer())
                        .addValue(KODE_HOVEDOMRADE, næringsgruppering.getKodeHovedområde())
                        .addValue(BESKRIVELSE_HOVEDOMRADE, næringsgruppering.getBeskrivelseHovedområde());

        return namedParameterJdbcTemplate.update(
                "insert into naringsgruppering (kode_5siffer, beskrivelse_5siffer, kode_4siffer, beskrivelse_4siffer, kode_3siffer, beskrivelse_3siffer, kode_2siffer, beskrivelse_2siffer, kode_hovedomrade, beskrivelse_hovedomrade)  " +
                        "values (:kode_5siffer, :beskrivelse_5siffer, :kode_4siffer, :beskrivelse_4siffer, :kode_3siffer, :beskrivelse_3siffer, :kode_2siffer, :beskrivelse_2siffer, :kode_hovedomrade, :beskrivelse_hovedomrade)",
                namedParameters
        );
    }
}
