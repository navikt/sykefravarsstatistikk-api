package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.utils;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Sykefraværsstatistikk;

import java.util.List;

public interface SykefraværsstatistikkIntegrasjonUtils {

    DeleteSykefraværsstatistikkFunction getDeleteFunction();

    BatchCreateSykefraværsstatistikkFunction getBatchCreateFunction(List<? extends Sykefraværsstatistikk> list);

}
