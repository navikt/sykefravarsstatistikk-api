package no.nav.tag.sykefravarsstatistikk.api.domain.autorisasjon;


import no.nav.tag.sykefravarsstatistikk.api.domain.Orgnr;

public class InnloggetNavAnsatt extends InnloggetBruker<NavIdent> {
    public InnloggetNavAnsatt(NavIdent identifikator) {
        super(identifikator);
    }

    @Override
    public boolean harTilgang(Orgnr orgnr) {
        return true;
    }
}
