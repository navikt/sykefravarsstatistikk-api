package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.importering

data class SlettOgOpprettResultat(var antallRadSlettet: Int = 0, var antallRadOpprettet: Int = 0) {

    companion object {

        fun tomtResultat(): SlettOgOpprettResultat {
            return SlettOgOpprettResultat(0, 0)
        }
    }
}
