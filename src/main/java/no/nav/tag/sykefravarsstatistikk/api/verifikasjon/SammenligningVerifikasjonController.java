package no.nav.tag.sykefravarsstatistikk.api.verifikasjon;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.tag.sykefravarsstatistikk.api.sammenligning.SammenligningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"dev", "local"})
@Protected
@RestController
@Slf4j
@RequestMapping(value = "/verifikasjon")
public class SammenligningVerifikasjonController {

    private final SammenligningRepository sykefravarprosentRepository;
    @Autowired
    public SammenligningVerifikasjonController(SammenligningRepository sykefravarprosentRepository){
        this.sykefravarprosentRepository = sykefravarprosentRepository;
    }

    @GetMapping(value = "/{orgnr}/sykefravarsprosent/{årstall}/{kvartal}")
    public Sykefraværprosent verifiserSykefraværsprosentUnderenhet(
            @PathVariable("orgnr") String orgnrStr,
            @PathVariable int årstall,
            @PathVariable int kvartal
    ) {
        return sykefravarprosentRepository.hentSykefraværprosentVirksomhet(
                årstall,
                kvartal,
                Underenhet.builder()
                        .orgnr(new Orgnr(orgnrStr))
                        .build()
        );
    }

    @GetMapping(value = "/{naring}/sykefravarsprosent/{årstall}/{kvartal}")
    public Sykefraværprosent verifiserSykefraværsprosentNæring(
            @PathVariable String naring,
            @PathVariable int årstall,
            @PathVariable int kvartal
    ) {
        return sykefravarprosentRepository.hentSykefraværprosentNæring(
                årstall,
                kvartal,
                new Næringskode5Siffer(naring, "")
        );
    }

    @GetMapping(value = "/{sektor}/sykefravarsprosent/{årstall}/{kvartal}")
    public Sykefraværprosent verifiserSykefraværsprosentSektor(
            @PathVariable String sektor,
            @PathVariable int årstall,
            @PathVariable int kvartal
    ) {
        return sykefravarprosentRepository.hentSykefraværprosentSektor(årstall, kvartal, sektor);
    }

    @GetMapping(value = "/sykefravarsprosent/{årstall}/{kvartal}")
    public Sykefraværprosent verifiserSykefraværsprosentSektor(
            @PathVariable int årstall,
            @PathVariable int kvartal
    ) {
        return sykefravarprosentRepository.hentSykefraværprosentLand(årstall, kvartal);
    }

}
