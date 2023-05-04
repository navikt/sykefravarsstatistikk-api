package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert

data class AggregertStatistikkDto(
    val prosentSiste4KvartalerTotalt: List<StatistikkDto> = listOf(),
    val prosentSiste4KvartalerGradert: List<StatistikkDto> = listOf(),
    val prosentSiste4KvartalerKorttid: List<StatistikkDto> = listOf(),
    val prosentSiste4KvartalerLangtid: List<StatistikkDto> = listOf(),
    val trendTotalt: List<StatistikkDto> = listOf(),
    val tapteDagsverkTotalt: List<StatistikkDto> = listOf(),
    val muligeDagsverkTotalt: List<StatistikkDto> = listOf(),
)