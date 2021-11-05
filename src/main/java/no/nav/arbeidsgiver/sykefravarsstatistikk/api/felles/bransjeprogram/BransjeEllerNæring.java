package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;

import java.util.Optional;

public class BransjeEllerNæring {
    private final Optional<Bransje> bransje;
    private final Optional<Næring> næring;


    public BransjeEllerNæring(Optional<Bransje> bransje, Optional<Næring> næring) {
        this.næring = næring;
        this.bransje = bransje;
    }

    public Statistikkategori getStatistikkategori() {
        if (bransje.isPresent()) {
            return Statistikkategori.BRANSJE;
        } else {
            return Statistikkategori.NÆRING;
        }
    }

    public boolean isBransje() {
        return bransje.isPresent();
    }

    public Bransje getBransje() {
        return bransje.orElseThrow();
    }

    public Næring getNæring() {
        return næring.orElseThrow();
    }
}
