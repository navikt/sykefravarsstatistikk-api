package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.common.SlettOgOpprettResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"local", "dev"})
@Protected
@RestController
@Slf4j
@RequestMapping(value = "provisjonering/import")
public class StatistikkImportController {

    private final StatistikkImportService service;

    @Autowired
    public StatistikkImportController(StatistikkImportService service){
        this.service = service;
    }


    @PostMapping(value = "/land/{årstall}/{kvartal}")
    public SlettOgOpprettResultat importSykefraværsstatistikkLand(@PathVariable int årstall, @PathVariable int kvartal) {
        return service.importSykefraværsstatistikkLand(new ÅrstallOgKvartal(årstall, kvartal));
    }

    @PostMapping(value = "/sektor/{årstall}/{kvartal}")
    public SlettOgOpprettResultat importSykefraværsstatistikkSektor(@PathVariable int årstall, @PathVariable int kvartal) {
        return service.importSykefraværsstatistikkSektor(new ÅrstallOgKvartal(årstall, kvartal));
    }

    @PostMapping(value = "/naring/{årstall}/{kvartal}")
    public SlettOgOpprettResultat importSykefraværsstatistikkNæring(@PathVariable int årstall, @PathVariable int kvartal) {
        return service.importSykefraværsstatistikkNæring(new ÅrstallOgKvartal(årstall, kvartal));
    }

    @PostMapping(value = "/naring5siffer/{årstall}/{kvartal}")
    public SlettOgOpprettResultat importSykefraværsstatistikkNæring5siffer(@PathVariable int årstall, @PathVariable int kvartal) {
        return service.importSykefraværsstatistikkNæring5siffer(new ÅrstallOgKvartal(årstall, kvartal));
    }

    @PostMapping(value = "/virksomhet/{årstall}/{kvartal}")
    public SlettOgOpprettResultat importSykefraværsstatistikkVirksomhet(@PathVariable int årstall, @PathVariable int kvartal) {
        return service.importSykefraværsstatistikkVirksomhet(new ÅrstallOgKvartal(årstall, kvartal));
    }

}
