package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.KildeTilVirksomhetsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
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
                orgnr = Orgnr(verdi = it.orgnr!!),
                navn = "Bedrift ${it.orgnr}",
                rectype = it.rectype,
                sektor = "3",
                næring = it.næring,
                næringskode = it.næringkode,
                årstallOgKvartal = årstallOgKvartal
            )
        }
    }
}