package no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.synkronisering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Virksomhetsklassifikasjon;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.OpprettEllerOppdaterResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Virksomhetsklassifikasjon;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils.NæringIntegrasjonUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils.SektorIntegrasjonUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils.VirksomhetsklassifikasjonIntegrasjonUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VirksomhetsklassifikasjonerSynkroniseringRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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
}
