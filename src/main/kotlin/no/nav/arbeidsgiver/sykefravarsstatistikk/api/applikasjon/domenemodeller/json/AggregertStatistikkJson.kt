package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.json

data class AggregertStatistikkJson(
    val prosentSiste4KvartalerTotalt: List<StatistikkJson> = listOf(),
    val prosentSiste4KvartalerGradert: List<StatistikkJson> = listOf(),
    val prosentSiste4KvartalerKorttid: List<StatistikkJson> = listOf(),
    val prosentSiste4KvartalerLangtid: List<StatistikkJson> = listOf(),
    val trendTotalt: List<StatistikkJson> = listOf(),
    val tapteDagsverkTotalt: List<StatistikkJson> = listOf(),
    val muligeDagsverkTotalt: List<StatistikkJson> = listOf(),
)