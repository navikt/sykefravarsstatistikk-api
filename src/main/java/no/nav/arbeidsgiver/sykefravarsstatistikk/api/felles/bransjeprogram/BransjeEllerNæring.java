package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.TREND_BRANSJE;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.TREND_NÆRING;

import io.vavr.control.Either;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;

public class BransjeEllerNæring {

    public final Either<Bransje, Næring> verdi;

    public BransjeEllerNæring(Bransje bransje) {
        this.verdi = Either.left(bransje);
    }

    public BransjeEllerNæring(Næring næring) {
        this.verdi = Either.right(næring);
    }

    public Statistikkategori getStatistikkategori() {
        if (verdi.isLeft()) {
            return Statistikkategori.BRANSJE;
        } else {
            return Statistikkategori.NÆRING;
        }
    }

    public Statistikkategori getTrendkategori() {
        return this.isBransje() ? TREND_BRANSJE : TREND_NÆRING;
    }


    public boolean isBransje() {
        return verdi.isLeft();
    }

    public Bransje getBransje() {
        return verdi.getLeft();
    }

    public String getVerdiSomString() {
        return (this.isBransje() ? verdi.getLeft() : verdi.get()).toString();
    }

    public Næring getNæring() {
        return verdi.get();
    }
}
