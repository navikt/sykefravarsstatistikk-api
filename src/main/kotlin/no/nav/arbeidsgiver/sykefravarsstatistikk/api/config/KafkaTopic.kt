package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Statistikkategori

enum class KafkaTopic(val navn: String) {
    SYKEFRAVARSSTATISTIKK_LAND_V1("arbeidsgiver.sykefravarsstatistikk-land-v1"),
    SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1("arbeidsgiver.sykefravarsstatistikk-virksomhet-v1"),
    SYKEFRAVARSSTATISTIKK_NARING_V1("arbeidsgiver.sykefravarsstatistikk-naring-v1"),
    SYKEFRAVARSSTATISTIKK_SEKTOR_V1("arbeidsgiver.sykefravarsstatistikk-sektor-v1"),
    SYKEFRAVARSSTATISTIKK_BRANSJE_V1("arbeidsgiver.sykefravarsstatistikk-bransje-v1"),
    SYKEFRAVARSSTATISTIKK_NARINGSKODE_V1("arbeidsgiver.sykefravarsstatistikk-naringskode-v1"),
    SYKEFRAVARSSTATISTIKK_METADATA_V1("arbeidsgiver.sykefravarsstatistikk-metadata-virksomhet-v1"),

    @Deprecated(
        "Skal erstattes av topics med spesifikke kategorier",
        level = DeprecationLevel.WARNING
    )
    SYKEFRAVARSSTATISTIKK_V1("arbeidsgiver.sykefravarsstatistikk-v1");

    companion object {

        fun from(statistikkategori: Statistikkategori): KafkaTopic? {
            return when (statistikkategori) {
                Statistikkategori.LAND -> SYKEFRAVARSSTATISTIKK_LAND_V1
                Statistikkategori.SEKTOR -> SYKEFRAVARSSTATISTIKK_SEKTOR_V1
                Statistikkategori.NÆRING -> SYKEFRAVARSSTATISTIKK_NARING_V1
                Statistikkategori.BRANSJE -> SYKEFRAVARSSTATISTIKK_BRANSJE_V1
                Statistikkategori.NÆRINGSKODE -> SYKEFRAVARSSTATISTIKK_NARINGSKODE_V1
                Statistikkategori.VIRKSOMHET -> SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1
                Statistikkategori.OVERORDNET_ENHET -> null
            }
        }


        fun toStringArray() = entries.map { it.navn }.toTypedArray()

    }
}

