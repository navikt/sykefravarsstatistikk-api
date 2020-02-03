package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk;

import org.springframework.stereotype.Component;

@Component
public class SykefraværprosentHistorikkService {
    private final KvartalsvisSykefraværprosentRepository kvartalsvisSykefraværprosentRepository;

    public SykefraværprosentHistorikkService(KvartalsvisSykefraværprosentRepository kvartalsvisSykefraværprosentRepository) {
        this.kvartalsvisSykefraværprosentRepository = kvartalsvisSykefraværprosentRepository;
    }

    public KvartalsvisSykefraværprosentHistorikk hentKvartalsvisSykefraværprosentHistorikkLand() {
        KvartalsvisSykefraværprosentHistorikk kvartalsvisSykefraværprosentHistorikk = new KvartalsvisSykefraværprosentHistorikk();
        kvartalsvisSykefraværprosentHistorikk.setLabel("Norge");
        kvartalsvisSykefraværprosentHistorikk.setKvartalsvisSykefraværProsent(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand());

        return  kvartalsvisSykefraværprosentHistorikk;
    }
}
