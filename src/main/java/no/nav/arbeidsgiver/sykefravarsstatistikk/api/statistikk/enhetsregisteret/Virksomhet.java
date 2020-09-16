package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;

public interface Virksomhet {
    public Orgnr getOrgnr();
    public String getNavn();
}
