package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

data class ÅrstallOgKvartal(
    val årstall: Int,
    val kvartal: Int
) : Comparable<ÅrstallOgKvartal> {

    init {
        require(!(kvartal > 4 || kvartal < 1)) { "Kvartal må være 1, 2, 3 eller 4" }
    }

    fun minusKvartaler(antallKvartaler: Int): ÅrstallOgKvartal {
        if (antallKvartaler < 0) {
            return plussKvartaler(-antallKvartaler)
        }
        var årstallOgKvartal = ÅrstallOgKvartal(årstall, kvartal)
        for (i in 0 until antallKvartaler) {
            årstallOgKvartal = årstallOgKvartal.forrigeKvartal()
        }
        return årstallOgKvartal
    }

    fun minusEttÅr(): ÅrstallOgKvartal {
        return ÅrstallOgKvartal(årstall - 1, kvartal)
    }

    fun plussKvartaler(antallKvartaler: Int): ÅrstallOgKvartal {
        if (antallKvartaler < 0) {
            return minusKvartaler(-antallKvartaler)
        }
        var årstallOgKvartal = ÅrstallOgKvartal(årstall, kvartal)
        for (i in 0 until antallKvartaler) {
            årstallOgKvartal = årstallOgKvartal.nesteKvartal()
        }
        return årstallOgKvartal
    }

    private fun forrigeKvartal(): ÅrstallOgKvartal {
        return if (kvartal == 1) {
            ÅrstallOgKvartal(årstall - 1, 4)
        } else {
            ÅrstallOgKvartal(årstall, kvartal - 1)
        }
    }

    private fun nesteKvartal(): ÅrstallOgKvartal {
        return if (kvartal == 4) {
            ÅrstallOgKvartal(årstall + 1, 1)
        } else {
            ÅrstallOgKvartal(årstall, kvartal + 1)
        }
    }

    override fun compareTo(other: ÅrstallOgKvartal): Int {
        return Comparator.comparing<ÅrstallOgKvartal, Int> { it.årstall }
            .thenComparing<Int> { it.kvartal }
            .compare(this, other)
    }

    override fun toString(): String {
        return "$kvartal. kvartal $årstall"
    }

    companion object {

        fun sisteFireKvartaler(årstallOgKvartal: ÅrstallOgKvartal): List<ÅrstallOgKvartal> =
            (0..3).map(årstallOgKvartal::minusKvartaler).sorted()


        fun range(fra: ÅrstallOgKvartal, til: ÅrstallOgKvartal): List<ÅrstallOgKvartal> {
            val årstallOgKvartal: MutableList<ÅrstallOgKvartal> = ArrayList()
            var i = fra
            while (i <= til) {
                årstallOgKvartal.add(i)
                i = i.plussKvartaler(1)
            }
            return årstallOgKvartal
        }
    }
}
