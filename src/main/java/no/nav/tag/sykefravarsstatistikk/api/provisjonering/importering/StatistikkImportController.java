package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkLand;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkSektor;
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
        return service.hentSykefraværsstatistikkLand(årstall, kvartal);
    }

    @GetMapping(value = "/verif/datavarehus/sykefravar/sektor/{årstall}/{kvartal}")
    public List<SykefraværsstatistikkSektor> hentSykefraværsstatistikkSektor(@PathVariable int årstall, @PathVariable int kvartal) {
        return service.hentSykefraværsstatistikkSektor(årstall, kvartal);
    }

    @PostMapping(value = "/land/{årstall}/{kvartal}")
    public String importSykefraværsstatistikkLand(@PathVariable int årstall, @PathVariable int kvartal) {
        service.importSykefraværsstatistikkLand(årstall, kvartal);
        return "OK";
    }

}
