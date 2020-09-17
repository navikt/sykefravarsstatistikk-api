package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OpprettEllerOppdaterResultat;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class KlassifikasjonerService {

    private final DatavarehusRepository datavarehusRepository;
    private final KlassifikasjonerRepository
            klassifikasjonerRepository;

    public KlassifikasjonerService(
            DatavarehusRepository datavarehusRepository,
            KlassifikasjonerRepository klassifikasjonerRepository
    ) {
        this.datavarehusRepository = datavarehusRepository;
        this.klassifikasjonerRepository = klassifikasjonerRepository;
    }

    public OpprettEllerOppdaterResultat populerSektorer() {
        List<Sektor> sektorer = datavarehusRepository.hentAlleSektorer();
        OpprettEllerOppdaterResultat resultat =
                opprettEllerOppdaterSektorer(sektorer);
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
                opprettEllerOppdaterNæringer(næringer);
        log.info(
                String.format(
                        "Import av næringer (med næringskode på 2 siffer) er ferdig. Antall opprettet: %d, antall oppdatert: %d",
                        resultat.getAntallRadOpprettet(),
                        resultat.getAntallRadOppdatert()
                )
        );
        return resultat;
    }

    public OpprettEllerOppdaterResultat opprettEllerOppdaterSektorer(
            List<Sektor> sektorerIDatavarehus) {
        return sektorerIDatavarehus
                .stream()
                .map(sektor -> opprettEllerOppdaterSektor(sektor))
                .reduce(new OpprettEllerOppdaterResultat(), OpprettEllerOppdaterResultat::add);
    }

    private OpprettEllerOppdaterResultat opprettEllerOppdaterSektor(
            Sektor sektor) {
        final OpprettEllerOppdaterResultat resultat = new OpprettEllerOppdaterResultat();

        klassifikasjonerRepository.hent(sektor, Klassifikasjonskilde.SEKTOR).ifPresentOrElse(
                eksisterendeSektor -> {
                    if (!eksisterendeSektor.equals(sektor)) {
                        klassifikasjonerRepository.oppdater(sektor, Klassifikasjonskilde.SEKTOR);
                        resultat.setAntallRadOppdatert(1);
                    }
                },
                () -> {
                    klassifikasjonerRepository.opprett(sektor, Klassifikasjonskilde.SEKTOR);
                    resultat.setAntallRadOpprettet(1);
                }
        );
        return resultat;
    }

    public OpprettEllerOppdaterResultat opprettEllerOppdaterNæringer(List<Næring> næringerIDatavarehus) {
        return næringerIDatavarehus
                .stream()
                .map(næring -> opprettEllerOppdaterNæring(næring))
                .reduce(new OpprettEllerOppdaterResultat(), OpprettEllerOppdaterResultat::add);
    }


    private OpprettEllerOppdaterResultat opprettEllerOppdaterNæring(
            Næring næring) {
        final OpprettEllerOppdaterResultat resultat = new OpprettEllerOppdaterResultat();

        klassifikasjonerRepository.hent(næring, Klassifikasjonskilde.NÆRING).ifPresentOrElse(
                eksisterendeNæring -> {
                    if (!eksisterendeNæring.equals(næring)) {
                        klassifikasjonerRepository.oppdater(næring, Klassifikasjonskilde.NÆRING);
                        resultat.setAntallRadOppdatert(1);
                    }
                },
                () -> {
                    klassifikasjonerRepository.opprett(næring, Klassifikasjonskilde.NÆRING);
                    resultat.setAntallRadOpprettet(1);
                }
        );
        return resultat;
    }

}
