package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.CreateSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;

public interface SykefraværsstatistikkIntegrasjonUtils {

    DeleteSykefraværsstatistikkFunction getDeleteFunction();
    CreateSykefraværsstatistikkFunction getCreateFunction();

}
