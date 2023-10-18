package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sykefraværsstatistikk

interface SykefraværsstatistikkIntegrasjonUtils {
    fun getDeleteFunction(): DeleteSykefraværsstatistikkFunction
    fun getBatchCreateFunction(
        statistikk: List<Sykefraværsstatistikk>
    ): BatchCreateSykefraværsstatistikkFunction
}
