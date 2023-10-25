package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene

enum class Varighetskategori(val kode: String?) {
    _1_DAG_TIL_7_DAGER("A"),
    _8_DAGER_TIL_16_DAGER("B"),
    _17_DAGER_TIL_8_UKER("C"),
    _8_UKER_TIL_20_UKER("D"),
    _20_UKER_TIL_39_UKER("E"),
    MER_ENN_39_UKER("F"),
    TOTAL("X"),
    UKJENT(null);

    fun erTotalvarighet(): Boolean {
        return kode == "X"
    }

    fun erKorttidVarighet(): Boolean {
        return when (kode) {
            "A", "B" -> true
            else -> false
        }
    }

    fun erLangtidVarighet(): Boolean {
        return when (kode) {
            "C", "D", "E", "F" -> true
            else -> false
        }
    }

    override fun toString(): String {
        return kode ?: "UKJENT"
    }

    companion object {
        fun fraKode(kode: String?): Varighetskategori =
            Varighetskategori.entries.find { it.kode == kode }
                ?: throw IllegalArgumentException("Det finnes ingen sykefraværsvarighet med kode $kode")
    }
}

