package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.*

@Data
@AllArgsConstructor
@NoArgsConstructor
class OpprettEllerOppdaterResultat {
    private var antallRadOpprettet = 0
    private var antallRadOppdatert = 0
    fun add(resultat: OpprettEllerOppdaterResultat): OpprettEllerOppdaterResultat {
        antallRadOpprettet = antallRadOpprettet + resultat.getAntallRadOpprettet()
        antallRadOppdatert = antallRadOppdatert + resultat.getAntallRadOppdatert()
        return this
    }
}
