package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.CreateVirksomhetsklassifikasjonFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.FetchVirksomhetsklassifikasjonFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.UpdateVirksomhetsklassifikasjonFunction;

public interface VirksomhetsklassifikasjonIntegrasjonUtils {

    FetchVirksomhetsklassifikasjonFunction getFetchFunction();

    CreateVirksomhetsklassifikasjonFunction getCreateFunction();

    UpdateVirksomhetsklassifikasjonFunction getUpdateFunction();

}
