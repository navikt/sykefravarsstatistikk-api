package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner.utils;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner.CreateVirksomhetsklassifikasjonFunction;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner.FetchVirksomhetsklassifikasjonFunction;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner.UpdateVirksomhetsklassifikasjonFunction;

public interface VirksomhetsklassifikasjonIntegrasjonUtils {

    FetchVirksomhetsklassifikasjonFunction getFetchFunction();

    CreateVirksomhetsklassifikasjonFunction getCreateFunction();

    UpdateVirksomhetsklassifikasjonFunction getUpdateFunction();

}
