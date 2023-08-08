package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.bransjeprogram;

import io.vavr.control.Either;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Næring;
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

  public boolean isBransje() {
    return verdi.isLeft();
  }

  public Bransje getBransje() {
    return verdi.getLeft();
  }

  public String navn() {
    return this.isBransje() ? verdi.getLeft().getNavn() : verdi.get().getNavn();
  }

  public Næring getNæring() {
    return verdi.get();
  }
}
