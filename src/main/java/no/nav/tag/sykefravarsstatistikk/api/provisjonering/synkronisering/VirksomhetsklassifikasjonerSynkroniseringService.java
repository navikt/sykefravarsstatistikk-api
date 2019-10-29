package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.domene.OpprettEllerOppdaterResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppe;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.DataverehusRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile({"local", "dev"})
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

    public void populerSektorer() {
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
    }

    public void populerNæringskoder() {
        populerNæringsgrupper();
        populerNæringer();
    }

    // TODO: DELETE ME --> bare til versifisering
    public List<Sektor> hentSektorer() {
        return datavarehusRepository.hentAlleSektorer();
    }


    private void populerNæringsgrupper() {

        List<Næringsgruppe> næringsgrupper = datavarehusRepository.hentAlleNæringsgrupper();
        OpprettEllerOppdaterResultat resultat =
                virksomhetsklassifikasjonerSynkroniseringRepository.opprettEllerOppdaterNæringsgrupper(næringsgrupper);
        log.info(
                String.format(
                        "Import av næringsgrupper er ferdig. Antall opprettet: %d, antall oppdatert: %d",
                        resultat.getAntallRadOpprettet(),
                        resultat.getAntallRadOppdatert()
                )
        );
    }

    private void populerNæringer() {

        List<Næring> næringer = datavarehusRepository.hentAlleNæringer();
        OpprettEllerOppdaterResultat resultat =
                virksomhetsklassifikasjonerSynkroniseringRepository.opprettEllerOppdaterNæringer(næringer);
        log.info(
                String.format(
                        "Import av næringer er ferdig. Antall opprettet: %d, antall oppdatert: %d",
                        resultat.getAntallRadOpprettet(),
                        resultat.getAntallRadOppdatert()
                )
        );
    }


}
