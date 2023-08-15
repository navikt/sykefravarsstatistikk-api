package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.statistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sykefraværsstatistikk

interface SykefraværsstatistikkIntegrasjonUtils {
    fun getDeleteFunction(): DeleteSykefraværsstatistikkFunction
    fun getBatchCreateFunction(
        list: List<Sykefraværsstatistikk>
    ): BatchCreateSykefraværsstatistikkFunction
}
