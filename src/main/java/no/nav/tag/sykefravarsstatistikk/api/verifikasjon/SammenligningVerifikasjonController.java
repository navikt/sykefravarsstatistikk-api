package no.nav.tag.sykefravarsstatistikk.api.verifikasjon;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.tag.sykefravarsstatistikk.api.sammenligning.SammenligningRepository;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.KlassifikasjonerRepository;
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
    private final KlassifikasjonerRepository klassifikasjonerRepository;

    @Autowired
    public SammenligningVerifikasjonController(SammenligningRepository sykefravarprosentRepository, KlassifikasjonerRepository klassifikasjonerRepository){
        this.sykefravarprosentRepository = sykefravarprosentRepository;
        this.klassifikasjonerRepository = klassifikasjonerRepository;
    }

    @GetMapping(value = "sykefravarsprosent/underenhet/{orgnr}/{årstall}/{kvartal}")
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
                        .navn(orgnrStr)
                        .build()
        );
    }

    @GetMapping(value = "/sykefravarsprosent/naring/{naring}/{årstall}/{kvartal}")
    public Sykefraværprosent verifiserSykefraværsprosentNæring(
            @PathVariable(name = "naring") String næringskode2Siffer,
            @PathVariable int årstall,
            @PathVariable int kvartal
    ) {
        Næring næring = klassifikasjonerRepository.hentNæring(næringskode2Siffer);

        return sykefravarprosentRepository.hentSykefraværprosentNæring(
                årstall,
                kvartal,
                new Næringskode5Siffer(
                        String.format("%s.000", næringskode2Siffer),
                        næring.getNavn()
                )
        );
    }

    @GetMapping(value = "/sykefravarsprosent/sektor/{sektor}/{årstall}/{kvartal}")
    public Sykefraværprosent verifiserSykefraværsprosentSektor(
            @PathVariable String sektor,
            @PathVariable int årstall,
            @PathVariable int kvartal
    ) {
        return sykefravarprosentRepository.hentSykefraværprosentSektor(årstall, kvartal, sektor);
    }

    @GetMapping(value = "/sykefravarsprosent/land/{årstall}/{kvartal}")
    public Sykefraværprosent verifiserSykefraværsprosentSektor(
            @PathVariable int årstall,
            @PathVariable int kvartal
    ) {
        return sykefravarprosentRepository.hentSykefraværprosentLand(årstall, kvartal);
    }

}
