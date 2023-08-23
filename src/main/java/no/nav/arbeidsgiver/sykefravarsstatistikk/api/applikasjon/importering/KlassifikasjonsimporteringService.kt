package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.OpprettEllerOppdaterResultat
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KlassifikasjonsimporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class KlassifikasjonsimporteringService(
    private val datavarehusRepository: DatavarehusRepository,
    private val klassifikasjonsimporteringRepository: KlassifikasjonsimporteringRepository
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun populerSektorer(): OpprettEllerOppdaterResultat {
        val sektorer = datavarehusRepository.hentAlleSektorer()
        val resultat = opprettEllerOppdaterSektor(sektorer)
        log.info(
            String.format(
                "Import av sektorer er ferdig. Antall opprettet: %d, antall oppdatert: %d",
                resultat.antallRadOpprettet, resultat.antallRadOppdatert
            )
        )
        return resultat
    }

    fun populerNæringskoder(): OpprettEllerOppdaterResultat {
        val næringer = datavarehusRepository.hentAlleNæringer()
        val resultat = opprettEllerOppdaterNæring(næringer)
        log.info(
            String.format(
                "Import av næringer (med næringskode på 2 siffer) er ferdig. Antall opprettet: %d, antall oppdatert: %d",
                resultat.antallRadOpprettet, resultat.antallRadOppdatert
            )
        )
        return resultat
    }

    fun opprettEllerOppdaterNæring(
        næringer: List<Næring>
    ): OpprettEllerOppdaterResultat {
        return næringer.stream()
            .map { klassifikasjon: Næring ->
                opprettEllerOppdaterNæring(
                    klassifikasjon
                )
            }
            .reduce(OpprettEllerOppdaterResultat()) { obj: OpprettEllerOppdaterResultat, other: OpprettEllerOppdaterResultat ->
                obj.add(
                    other
                )
            }
    }

    fun opprettEllerOppdaterSektor(
        sektorer: List<Sektor>
    ): OpprettEllerOppdaterResultat {
        return sektorer.stream()
            .map { klassifikasjon: Sektor ->
                opprettEllerOppdaterSektor(
                    klassifikasjon
                )
            }
            .reduce(OpprettEllerOppdaterResultat()) { obj: OpprettEllerOppdaterResultat, other: OpprettEllerOppdaterResultat ->
                obj.add(
                    other
                )
            }
    }

    private fun opprettEllerOppdaterNæring(
        næring: Næring
    ): OpprettEllerOppdaterResultat {
        var resultat = OpprettEllerOppdaterResultat()
        klassifikasjonsimporteringRepository
            .hentNæring(næring)
            .ifPresentOrElse(
                { eksisterendeKlassifikasjon: Næring ->
                    if (eksisterendeKlassifikasjon != næring) {
                        klassifikasjonsimporteringRepository.oppdaterNæring(
                            næring
                        )
                        resultat = OpprettEllerOppdaterResultat(0, 1)
                    }
                }
            ) {
                klassifikasjonsimporteringRepository.opprettNæring(
                    næring
                )
                resultat = OpprettEllerOppdaterResultat(1, 0)
            }
        return resultat
    }

    private fun opprettEllerOppdaterSektor(
        sektor: Sektor
    ): OpprettEllerOppdaterResultat {
        var resultat = OpprettEllerOppdaterResultat()
        klassifikasjonsimporteringRepository
            .hentSektor(sektor)
            .ifPresentOrElse(
                { eksisterendeKlassifikasjon: Sektor ->
                    if (eksisterendeKlassifikasjon != sektor) {
                        klassifikasjonsimporteringRepository.oppdaterSektor(
                            sektor
                        )
                        resultat = OpprettEllerOppdaterResultat(0, 1)
                    }
                }
            ) {
                klassifikasjonsimporteringRepository.opprettSektor(
                    sektor
                )
                resultat = OpprettEllerOppdaterResultat(1, 0)
            }
        return resultat
    }
}
