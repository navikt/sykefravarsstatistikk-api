package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

enum class Varighetskategori(@JvmField val kode: String) {
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
        return kode
    }

    companion object {
        private val FRA_KODE: MutableMap<String, Varighetskategori> = HashMap()

        init {
            for (varighet in entries) {
                FRA_KODE[varighet.kode] = varighet
            }
        }

        @JvmStatic
        fun fraKode(kode: String): Varighetskategori? {
            return if (FRA_KODE.containsKey(kode)) {
                FRA_KODE[kode]
            } else {
                throw IllegalArgumentException("Det finnes ingen sykefraværsvarighet med kode $kode")
            }
        }
    }
}
