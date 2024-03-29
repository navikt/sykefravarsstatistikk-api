package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene

enum class Varighetskategori(val kode: Char?) {
    _1_DAG_TIL_7_DAGER('A'),
    _8_DAGER_TIL_16_DAGER('B'),
    _17_DAGER_TIL_8_UKER('C'),
    _8_UKER_TIL_20_UKER('D'),
    _20_UKER_TIL_39_UKER('E'),
    MER_ENN_39_UKER('F'),
    TOTAL('X');


    override fun toString(): String {
        return kode.toString()
    }

    companion object {
        val kortidsvarigheter = listOf(
            TOTAL, _1_DAG_TIL_7_DAGER,
            _8_DAGER_TIL_16_DAGER
        )
        val langtidsvarigheter = listOf(
            TOTAL,
            _17_DAGER_TIL_8_UKER,
            _8_UKER_TIL_20_UKER,
            _20_UKER_TIL_39_UKER,
            MER_ENN_39_UKER
        )
    }
}

