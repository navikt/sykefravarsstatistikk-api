package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.importering.integrasjon.utils;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.Sykefraværsstatistikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.importering.integrasjon.BatchCreateSykefraværsstatistikkFunction;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;

import java.util.List;

public interface SykefraværsstatistikkIntegrasjonUtils {

    DeleteSykefraværsstatistikkFunction getDeleteFunction();

    BatchCreateSykefraværsstatistikkFunction getBatchCreateFunction(List<? extends Sykefraværsstatistikk> list);

}
