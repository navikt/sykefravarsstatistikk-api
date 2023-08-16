package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Klassifikasjonskilde
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.OpprettEllerOppdaterResultat
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Virksomhetsklassifikasjon
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
        val resultat = opprettEllerOppdaterVirksomhetsklassifikasjoner(sektorer, Klassifikasjonskilde.SEKTOR)
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
        val resultat = opprettEllerOppdaterVirksomhetsklassifikasjoner(næringer, Klassifikasjonskilde.NÆRING)
        log.info(
            String.format(
                "Import av næringer (med næringskode på 2 siffer) er ferdig. Antall opprettet: %d, antall oppdatert: %d",
                resultat.antallRadOpprettet, resultat.antallRadOppdatert
            )
        )
        return resultat
    }

    fun opprettEllerOppdaterVirksomhetsklassifikasjoner(
        virksomhetsklassifikasjonerIDatavarehus: List<Virksomhetsklassifikasjon>,
        klassifikasjonskilde: Klassifikasjonskilde
    ): OpprettEllerOppdaterResultat {
        return virksomhetsklassifikasjonerIDatavarehus.stream()
            .map { klassifikasjon: Virksomhetsklassifikasjon ->
                opprettEllerOppdater(
                    klassifikasjon,
                    klassifikasjonskilde
                )
            }
            .reduce(OpprettEllerOppdaterResultat()) { obj: OpprettEllerOppdaterResultat, other: OpprettEllerOppdaterResultat ->
                obj.add(
                    other
                )
            }
    }

    private fun opprettEllerOppdater(
        virksomhetsklassifikasjon: Virksomhetsklassifikasjon,
        klassifikasjonskilde: Klassifikasjonskilde
    ): OpprettEllerOppdaterResultat {
        var resultat = OpprettEllerOppdaterResultat()
        klassifikasjonsimporteringRepository
            .hent(virksomhetsklassifikasjon, klassifikasjonskilde)
            .ifPresentOrElse(
                { eksisterendeKlassifikasjon: Virksomhetsklassifikasjon ->
                    if (eksisterendeKlassifikasjon != virksomhetsklassifikasjon) {
                        klassifikasjonsimporteringRepository.oppdater(
                            virksomhetsklassifikasjon, klassifikasjonskilde
                        )
                        resultat = OpprettEllerOppdaterResultat(0, 1)
                    }
                }
            ) {
                klassifikasjonsimporteringRepository.opprett(
                    virksomhetsklassifikasjon, klassifikasjonskilde
                )
                resultat = OpprettEllerOppdaterResultat(1, 0)
            }
        return resultat
    }
}
