package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransjeprogram;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BransjeEllerNæringService {

    public BransjeEllerNæring finnBransje(Virksomhet virksomhet) {
        Optional<Bransje> bransje = Bransjeprogram.finnBransje(virksomhet);

        return bransje
                .map(BransjeEllerNæring::new)
                .orElseGet(
                        () ->
                        {
                            String kode = virksomhet.getNæringskode().getNæring().getTosifferIdentifikator();
                            return new BransjeEllerNæring(
                                    new Næring(kode));
                        });
    }

}
