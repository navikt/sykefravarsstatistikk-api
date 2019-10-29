package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering;

import no.nav.tag.sykefravarsstatistikk.api.domene.OpprettEllerOppdaterResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppe;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Virksomhetsklassifikasjon;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile({"local", "dev"})
@Component
public class VirksomhetsklassifikasjonerSynkroniseringRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public VirksomhetsklassifikasjonerSynkroniseringRepository(
      @Qualifier("sykefravarsstatistikkJdbcTemplate")
          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  public OpprettEllerOppdaterResultat opprettEllerOppdaterSektorer(
      List<Sektor> sektorerIDatavarehus) {
    final OpprettEllerOppdaterResultat sluttResultat = new OpprettEllerOppdaterResultat();

    sektorerIDatavarehus.stream()
        .forEach(
            sektor -> {
              OpprettEllerOppdaterResultat result =
                  opprettEllerOppdater(
                      sektor,
                      SektorIntegrasjonUtils.getHentSektorFunction(namedParameterJdbcTemplate),
                      SektorIntegrasjonUtils.getCreateSektorFunction(namedParameterJdbcTemplate),
                      SektorIntegrasjonUtils.getUpdateSektorFunction(namedParameterJdbcTemplate));
              sluttResultat.add(result);
            });

    return sluttResultat;
  }

  public OpprettEllerOppdaterResultat opprettEllerOppdaterNæringsgrupper(
      List<Næringsgruppe> næringsgrupper) {
    final OpprettEllerOppdaterResultat sluttResultat = new OpprettEllerOppdaterResultat();

    næringsgrupper.stream()
        .forEach(
            næringsgruppe -> {
              OpprettEllerOppdaterResultat result =
                  opprettEllerOppdater(
                      næringsgruppe,
                      NæringsgruppeIntegrasjonUtils.getHentNæringsgruppeFunction(
                          namedParameterJdbcTemplate),
                      NæringsgruppeIntegrasjonUtils.getCreateNæringsgruppeFunction(
                          namedParameterJdbcTemplate),
                      NæringsgruppeIntegrasjonUtils.getUpdateNæringsgruppeFunction(
                          namedParameterJdbcTemplate));
              sluttResultat.add(result);
            });

    return sluttResultat;
  }

  public OpprettEllerOppdaterResultat opprettEllerOppdaterNæringer(List<Næring> næringer) {
    final OpprettEllerOppdaterResultat sluttResultat = new OpprettEllerOppdaterResultat();

    næringer.stream()
        .forEach(
            næring -> {
              OpprettEllerOppdaterResultat result =
                  opprettEllerOppdater(
                      næring,
                      NæringIntegrasjonUtils.getHentNæringFunction(namedParameterJdbcTemplate),
                      NæringIntegrasjonUtils.getCreateNæringFunction(namedParameterJdbcTemplate),
                      NæringIntegrasjonUtils.getUpdateNæringFunction(namedParameterJdbcTemplate));
              sluttResultat.add(result);
            });

    return sluttResultat;
  }

  private OpprettEllerOppdaterResultat opprettEllerOppdater(
      Virksomhetsklassifikasjon virksomhetsklassifikasjon,
      FetchVirksomhetsklassifikasjonFunction fetchFunction,
      CreateVirksomhetsklassifikasjonFunction createFunction,
      UpdateVirksomhetsklassifikasjonFunction updateFunction) {
    final OpprettEllerOppdaterResultat resultat = new OpprettEllerOppdaterResultat();

    fetchFunction
        .apply(virksomhetsklassifikasjon)
        .ifPresentOrElse(
            eksisterende -> {
              if (!eksisterende.equals(virksomhetsklassifikasjon)) {
                updateFunction.apply(eksisterende, virksomhetsklassifikasjon);
                resultat.setAntallRadOppdatert(1);
              }
            },
            () -> {
              createFunction.apply(virksomhetsklassifikasjon);
              resultat.setAntallRadOpprettet(1);
            });

    return resultat;
  }
}
