package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
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
        if (kanLagreBesøksdata(sammenligningEvent.getUnderenhet(), sammenligningEvent.getSammenligning())) {
            besøksstatistikkRepository.lagreBesøkFraStorVirksomhet(
                    sammenligningEvent.getEnhet(),
                    sammenligningEvent.getSsbSektor(),
                    sammenligningEvent.getNæring5siffer(),
                    sammenligningEvent.getNæring2siffer(),
                    sammenligningEvent.getSammenligning(),
                    sammenligningEvent.getStatistikkId()
            );
        } else {
            besøksstatistikkRepository.lagreBesøkFraLitenVirksomhet(sammenligningEvent.getStatistikkId());
        }
    }


    private boolean kanLagreBesøksdata(Underenhet underenhet, Sammenligning sammenligning) {
        return !sammenligning.getVirksomhet().isErMaskert()
                && underenhet.getAntallAnsatte() >= MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
    }
}
