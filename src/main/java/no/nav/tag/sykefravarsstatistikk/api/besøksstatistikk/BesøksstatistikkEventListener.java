package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent.MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;

@Slf4j
@Component
public class BesøksstatistikkEventListener {

    private final BesøksstatistikkRepository besøksstatistikkRepository;

    public BesøksstatistikkEventListener(BesøksstatistikkRepository besøksstatistikkRepository) {
        this.besøksstatistikkRepository = besøksstatistikkRepository;
    }

    @EventListener
    public void onSammenligningUtsendt(SammenligningEvent sammenligningEvent) {
        if (sessionHarAlleredeBlittRegistrert(sammenligningEvent)) {
            return;
        }

        if (kanLagreBesøksdata(sammenligningEvent)) {
            besøksstatistikkRepository.lagreBesøkFraStorVirksomhet(
                    sammenligningEvent.getEnhet(),
                    sammenligningEvent.getSsbSektor(),
                    sammenligningEvent.getNæring5siffer(),
                    sammenligningEvent.getNæring2siffer(),
                    sammenligningEvent.getSammenligning(),
                    sammenligningEvent.getSessionId()
            );
        } else {
            besøksstatistikkRepository.lagreBesøkFraLitenVirksomhet(sammenligningEvent.getSessionId());
        }
    }

    private boolean sessionHarAlleredeBlittRegistrert(SammenligningEvent event) {
        return besøksstatistikkRepository.sessionIdEksisterer(event.getSessionId());
    }

    private boolean kanLagreBesøksdata(SammenligningEvent event) {
        return !event.getSammenligning().getVirksomhet().isErMaskert()
                && event.getUnderenhet().getAntallAnsatte() >= MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
    }
}
