package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.common.SlettOgOpprettResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping(value = "/verif/datavarehus/sykefravar/land/{årstall}/{kvartal}")
    public List<SykefraværsstatistikkLand> hentSykefraværsstatistikkLand(@PathVariable int årstall, @PathVariable int kvartal) {
        return service.hentSykefraværsstatistikkLand(new ÅrstallOgKvartal(årstall, kvartal));
    }

    @GetMapping(value = "/verif/datavarehus/sykefravar/sektor/{årstall}/{kvartal}")
    public List<SykefraværsstatistikkSektor> hentSykefraværsstatistikkSektor(@PathVariable int årstall, @PathVariable int kvartal) {
        return service.hentSykefraværsstatistikkSektor(new ÅrstallOgKvartal(årstall, kvartal));
    }

    @GetMapping(value = "/verif/datavarehus/sykefravar/naring/{årstall}/{kvartal}")
    public List<SykefraværsstatistikkNæring> hentSykefraværsstatistikkNæring(@PathVariable int årstall, @PathVariable int kvartal) {
        return service.hentSykefraværsstatistikkNæring(new ÅrstallOgKvartal(årstall, kvartal));
    }

    @GetMapping(value = "/verif/datavarehus/sykefravar/virksomhet/{årstall}/{kvartal}")
    public List<SykefraværsstatistikkVirksomhet> hentSykefraværsstatistikkVirksomhet(@PathVariable int årstall, @PathVariable int kvartal) {
        return service.hentSykefraværsstatistikkVirksomhet(new ÅrstallOgKvartal(årstall, kvartal));
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

    @PostMapping(value = "/virksomhet/{årstall}/{kvartal}")
    public SlettOgOpprettResultat importSykefraværsstatistikkVirksomhet(@PathVariable int årstall, @PathVariable int kvartal) {
        return service.importSykefraværsstatistikkVirksomhet(new ÅrstallOgKvartal(årstall, kvartal));
    }

}
