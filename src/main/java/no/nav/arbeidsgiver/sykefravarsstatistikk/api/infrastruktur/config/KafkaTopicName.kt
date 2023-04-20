package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori

enum class KafkaTopicName(val topic: String) {
    SYKEFRAVARSSTATISTIKK_LAND_V1("arbeidsgiver.sykefravarsstatistikk-land-v1"),
    SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1("arbeidsgiver.sykefravarsstatistikk-virksomhet-v1"),
    SYKEFRAVARSSTATISTIKK_NARING_V1("arbeidsgiver.sykefravarsstatistikk-naring-v1"),
    SYKEFRAVARSSTATISTIKK_SEKTOR_V1("arbeidsgiver.sykefravarsstatistikk-sektor-v1"),
    SYKEFRAVARSSTATISTIKK_BRANSJE_V1("arbeidsgiver.sykefravarsstatistikk-bransje-v1"),

    @Deprecated("Skal erstattes av topics med spesifikke kategorier", level = DeprecationLevel.WARNING)
    SYKEFRAVARSSTATISTIKK_V1("arbeidsgiver.sykefravarsstatistikk-v1");

    companion object {
        @JvmStatic
        fun from(statistikkategori: Statistikkategori): KafkaTopicName {
            return when (statistikkategori) {
                Statistikkategori.LAND -> SYKEFRAVARSSTATISTIKK_LAND_V1
                Statistikkategori.SEKTOR -> SYKEFRAVARSSTATISTIKK_SEKTOR_V1
                Statistikkategori.NÆRING -> SYKEFRAVARSSTATISTIKK_NARING_V1
                Statistikkategori.VIRKSOMHET -> SYKEFRAVARSSTATISTIKK_VIRKSOMHET_V1
                Statistikkategori.BRANSJE -> SYKEFRAVARSSTATISTIKK_BRANSJE_V1
                Statistikkategori.OVERORDNET_ENHET,
                Statistikkategori.NÆRING5SIFFER -> throw NotImplementedError()
            }
        }

        @JvmStatic
        fun toStringArray() = KafkaTopicName.values().map { it.topic }.toTypedArray()

    }
}

