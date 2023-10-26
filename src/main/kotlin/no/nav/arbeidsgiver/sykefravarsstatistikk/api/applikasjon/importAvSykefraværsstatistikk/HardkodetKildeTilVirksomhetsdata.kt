package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.KildeTilVirksomhetsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev")
@Primary
class HardkodetKildeTilVirksomhetsdata : KildeTilVirksomhetsdata {
    override fun hentVirksomheter(årstallOgKvartal: ÅrstallOgKvartal): List<Orgenhet> {
        return SykefraværsstatistikkImporteringUtils.genererSykefraværsstatistikkVirksomhetMedGradering(årstallOgKvartal).map {
            Orgenhet(
                orgnr = Orgnr(verdi = it.orgnr),
                navn = "Bedrift ${it.orgnr}",
                rectype = it.rectype,
                sektor = Sektor.PRIVAT,
                næring = it.næring,
                næringskode = it.næringkode,
                årstallOgKvartal = årstallOgKvartal
            )
        }
    }
}