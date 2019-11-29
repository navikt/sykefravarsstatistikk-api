package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BesøksstatistikkService {

    @EventListener
    public void sammenligning(SammenligningEvent sammenligningEvent) {
        log.info(sammenligningEvent.toString());
    }
}
