package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.synkronisering;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.DatavarehusRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OpprettEllerOppdaterResultat;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class VirksomhetsklassifikasjonerSynkroniseringService {

    private final DatavarehusRepository datavarehusRepository;
    private final VirksomhetsklassifikasjonerSynkroniseringRepository
            virksomhetsklassifikasjonerSynkroniseringRepository;

    public VirksomhetsklassifikasjonerSynkroniseringService(
            DatavarehusRepository datavarehusRepository,
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

}
