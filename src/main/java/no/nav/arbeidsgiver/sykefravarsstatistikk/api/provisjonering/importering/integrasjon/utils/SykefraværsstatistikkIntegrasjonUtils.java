package no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.Sykefraværsstatistikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.Sykefraværsstatistikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.BatchCreateSykefraværsstatistikkFunction;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;

import java.util.List;

public interface SykefraværsstatistikkIntegrasjonUtils {

    DeleteSykefraværsstatistikkFunction getDeleteFunction();

    BatchCreateSykefraværsstatistikkFunction getBatchCreateFunction(List<? extends Sykefraværsstatistikk> list);

}
