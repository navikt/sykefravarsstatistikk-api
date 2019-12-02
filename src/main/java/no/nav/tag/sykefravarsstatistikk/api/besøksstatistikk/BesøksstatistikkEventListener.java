package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BesøksstatistikkEventListener {

    private final BesøksstatistikkRepository besøksstatistikkRepository;

    public BesøksstatistikkEventListener(BesøksstatistikkRepository besøksstatistikkRepository) {
        this.besøksstatistikkRepository = besøksstatistikkRepository;
    }

    @EventListener
    public void sammenligning(SammenligningEvent sammenligningEvent) {
        besøksstatistikkRepository.loggBesøk(
                sammenligningEvent.getUnderenhet(),
                sammenligningEvent.getEnhet(),
                sammenligningEvent.getSsbSektor(),
                sammenligningEvent.getNæring(),
                sammenligningEvent.getSammenligning()
        );
    }
}
