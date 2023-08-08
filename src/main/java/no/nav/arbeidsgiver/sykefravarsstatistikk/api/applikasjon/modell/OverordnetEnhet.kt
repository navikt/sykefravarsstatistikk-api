package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell

data class OverordnetEnhet(
    override var orgnr: Orgnr,
    override var navn: String? = null,
    override var næringskode: Næringskode5Siffer? = null,
    var institusjonellSektorkode: InstitusjonellSektorkode? = null,
    var antallAnsatte: Int? = 0
) : Virksomhet {
    @Deprecated("Ikke bruk builder")
    class Builder {
        val overordnetEnhet = OverordnetEnhet(Orgnr("123"))

        fun orgnr(orgnr: Orgnr) = apply {
            overordnetEnhet.orgnr = orgnr
        }
        fun navn(navn: String?) = apply {
            overordnetEnhet.navn = navn
        }
        fun næringskode(næringskode: Næringskode5Siffer?) = apply {
            overordnetEnhet.næringskode = næringskode
        }
        fun institusjonellSektorkode(institusjonellSektorkode: InstitusjonellSektorkode?) = apply {
            overordnetEnhet.institusjonellSektorkode = institusjonellSektorkode
        }
        fun antallAnsatte(antallAnsatte: Int?) = apply {
            overordnetEnhet.antallAnsatte = antallAnsatte
        }
        fun build() = overordnetEnhet
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}