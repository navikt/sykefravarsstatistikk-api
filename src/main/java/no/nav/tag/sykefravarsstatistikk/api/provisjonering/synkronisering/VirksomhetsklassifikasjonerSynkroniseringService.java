package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.common.OpprettEllerOppdaterResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppering;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.DataverehusRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class VirksomhetsklassifikasjonerSynkroniseringService {

    private final DataverehusRepository datavarehusRepository;
    private final VirksomhetsklassifikasjonerSynkroniseringRepository
            virksomhetsklassifikasjonerSynkroniseringRepository;

    public VirksomhetsklassifikasjonerSynkroniseringService(
            DataverehusRepository datavarehusRepository,
            VirksomhetsklassifikasjonerSynkroniseringRepository virksomhetsklassifikasjonerSynkroniseringRepository
    ) {
        this.datavarehusRepository = datavarehusRepository;
        this.virksomhetsklassifikasjonerSynkroniseringRepository = virksomhetsklassifikasjonerSynkroniseringRepository;
    }

    public OpprettEllerOppdaterResultat populerSektorer() {
        List<Sektor> sektorer = datavarehusRepository.hentAlleSektorer();
        OpprettEllerOppdaterResultat resultat =
                virksomhetsklassifikasjonerSynkroniseringRepository.opprettEllerOppdaterSektorer(sektorer);
        log.info(
                String.format(
                        "Import av sektorer er ferdig. Antall opprettet: %d, antall oppdatert: %d",
                        resultat.getAntallRadOpprettet(),
                        resultat.getAntallRadOppdatert()
                )
        );
        return resultat;
    }

    public OpprettEllerOppdaterResultat populerNæringskoder() {

        List<Næring> næringer = datavarehusRepository.hentAlleNæringer();
        OpprettEllerOppdaterResultat resultat =
                virksomhetsklassifikasjonerSynkroniseringRepository.opprettEllerOppdaterNæringer(næringer);
        log.info(
                String.format(
                        "Import av næringer (med næringskode på 2 siffer) er ferdig. Antall opprettet: %d, antall oppdatert: %d",
                        resultat.getAntallRadOpprettet(),
                        resultat.getAntallRadOppdatert()
                )
        );
        return resultat;
    }

    public OpprettEllerOppdaterResultat populerNæringsgrupperinger() {
        List<Næringsgruppering> næringsgrupperinger = datavarehusRepository.hentAlleNæringsgrupperinger();
        OpprettEllerOppdaterResultat resultat =
                virksomhetsklassifikasjonerSynkroniseringRepository.opprettEllerOppdaterNæringsgrupperinger(næringsgrupperinger);
        log.info(
                String.format(
                        "Import av fullstendige næringsgrupperinger er ferdig. Antall opprettet: %d, antall oppdatert: %d",
                        resultat.getAntallRadOpprettet(),
                        resultat.getAntallRadOppdatert()
                )
        );
        return resultat;
    }

}
