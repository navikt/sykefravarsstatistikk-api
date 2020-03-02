package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;

public interface Virksomhet {
    public Orgnr getOrgnr();
    public String getNavn();
}
