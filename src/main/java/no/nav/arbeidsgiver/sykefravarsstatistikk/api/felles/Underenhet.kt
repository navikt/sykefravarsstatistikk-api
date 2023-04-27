package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles

data class Underenhet(
    override var orgnr: Orgnr,
    var overordnetEnhetOrgnr: Orgnr? = null,
    override var navn: String? = null,
    override var næringskode: Næringskode5Siffer? = null,
    var antallAnsatte: Int? = null,
) : Virksomhet {
    @Deprecated("Ikke bruk builder")
    class Builder {
        val underenhet = Underenhet(Orgnr("123"))

        fun orgnr(orgnr: Orgnr) = apply {
            underenhet.orgnr = orgnr
        }
        fun overordnetEnhetOrgnr(overordnetEnhetOrgnr: Orgnr?) = apply {
            underenhet.overordnetEnhetOrgnr = overordnetEnhetOrgnr
        }
        fun navn(navn: String?) = apply {
            underenhet.navn = navn
        }
        fun næringskode(næringskode: Næringskode5Siffer?) = apply {
            underenhet.næringskode = næringskode
        }
        fun antallAnsatte(antallAnsatte: Int?) = apply {
            underenhet.antallAnsatte = antallAnsatte
        }
        fun build() = underenhet
    }
    companion object {
        @JvmStatic
        fun builder(): Builder {
           return Builder()
        }
    }
}