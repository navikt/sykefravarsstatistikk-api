package no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;

public interface Virksomhet {
    public Orgnr getOrgnr();
    public String getNavn();
}
