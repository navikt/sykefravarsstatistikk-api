package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.Sykefraværsstatistikk;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.BatchCreateSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;

import java.util.List;

public interface SykefraværsstatistikkIntegrasjonUtils {

    DeleteSykefraværsstatistikkFunction getDeleteFunction();

    BatchCreateSykefraværsstatistikkFunction getBatchCreateFunction(List<? extends Sykefraværsstatistikk> list);

}
