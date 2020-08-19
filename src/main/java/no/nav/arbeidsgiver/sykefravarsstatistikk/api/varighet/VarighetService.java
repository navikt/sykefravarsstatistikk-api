package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class VarighetService {

    private final KvartalsvisSykefraværVarighetRepository kvartalsvisSykefraværVarighetRepository;

    public VarighetService(
            KvartalsvisSykefraværVarighetRepository kvartalsvisSykefraværVarighetRepository) {
        this.kvartalsvisSykefraværVarighetRepository = kvartalsvisSykefraværVarighetRepository;
    }


    public LangtidOgKorttidsSykefraværshistorikk hentLangtidOgKorttidsSykefraværshistorikk(Underenhet underenhet) {

        List<KvartalsvisSykefraværMedVarighet> sykefraværVarighet = kvartalsvisSykefraværVarighetRepository.hentKvartalsvisSykefraværMedVarighet(
                underenhet);

        return null;
    }
}
