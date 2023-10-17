package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class SlettOgOpprettResultat(var antallRadSlettet: Int = 0, var antallRadOpprettet: Int = 0) {
    fun add(resultat: SlettOgOpprettResultat) {
        antallRadOpprettet += resultat.antallRadOpprettet
        antallRadSlettet += resultat.antallRadSlettet
    }

    companion object {

        fun tomtResultat(): SlettOgOpprettResultat {
            return SlettOgOpprettResultat(0, 0)
        }
    }
}
