package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.OpprettEllerOppdaterResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Virksomhetsklassifikasjon;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Klassifikasjonskilde;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KlassifikasjonsimporteringRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class KlassifikasjonsimporteringService {

  private final DatavarehusRepository datavarehusRepository;
  private final KlassifikasjonsimporteringRepository klassifikasjonsimporteringRepository;

  public KlassifikasjonsimporteringService(
      DatavarehusRepository datavarehusRepository,
      KlassifikasjonsimporteringRepository klassifikasjonsimporteringRepository) {
    this.datavarehusRepository = datavarehusRepository;
    this.klassifikasjonsimporteringRepository = klassifikasjonsimporteringRepository;
  }

  public OpprettEllerOppdaterResultat populerSektorer() {
    List<Sektor> sektorer = datavarehusRepository.hentAlleSektorer();
    OpprettEllerOppdaterResultat resultat =
        opprettEllerOppdaterVirksomhetsklassifikasjoner(sektorer, Klassifikasjonskilde.SEKTOR);
    log.info(
        String.format(
            "Import av sektorer er ferdig. Antall opprettet: %d, antall oppdatert: %d",
            resultat.getAntallRadOpprettet(), resultat.getAntallRadOppdatert()));
    return resultat;
  }

  public OpprettEllerOppdaterResultat populerNæringskoder() {

    List<Næring> næringer = datavarehusRepository.hentAlleNæringer();
    OpprettEllerOppdaterResultat resultat =
        opprettEllerOppdaterVirksomhetsklassifikasjoner(næringer, Klassifikasjonskilde.NÆRING);
    log.info(
        String.format(
            "Import av næringer (med næringskode på 2 siffer) er ferdig. Antall opprettet: %d, antall oppdatert: %d",
            resultat.getAntallRadOpprettet(), resultat.getAntallRadOppdatert()));
    return resultat;
  }

  public OpprettEllerOppdaterResultat opprettEllerOppdaterVirksomhetsklassifikasjoner(
      List<? extends Virksomhetsklassifikasjon> virksomhetsklassifikasjonerIDatavarehus,
      Klassifikasjonskilde klassifikasjonskilde) {
    return virksomhetsklassifikasjonerIDatavarehus.stream()
        .map(klassifikasjon -> opprettEllerOppdater(klassifikasjon, klassifikasjonskilde))
        .reduce(new OpprettEllerOppdaterResultat(), OpprettEllerOppdaterResultat::add);
  }

  private OpprettEllerOppdaterResultat opprettEllerOppdater(
      Virksomhetsklassifikasjon virksomhetsklassifikasjon,
      Klassifikasjonskilde klassifikasjonskilde) {
    final OpprettEllerOppdaterResultat resultat = new OpprettEllerOppdaterResultat();

    klassifikasjonsimporteringRepository
        .hent(virksomhetsklassifikasjon, klassifikasjonskilde)
        .ifPresentOrElse(
            eksisterendeKlassifikasjon -> {
              if (!eksisterendeKlassifikasjon.equals(virksomhetsklassifikasjon)) {
                klassifikasjonsimporteringRepository.oppdater(
                    virksomhetsklassifikasjon, klassifikasjonskilde);
                resultat.setAntallRadOppdatert(1);
              }
            },
            () -> {
              klassifikasjonsimporteringRepository.opprett(
                  virksomhetsklassifikasjon, klassifikasjonskilde);
              resultat.setAntallRadOpprettet(1);
            });
    return resultat;
  }
}
