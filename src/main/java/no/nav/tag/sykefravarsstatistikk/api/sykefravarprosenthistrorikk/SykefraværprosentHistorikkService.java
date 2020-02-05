package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.SektorMappingService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SykefraværprosentHistorikkService {
    private final KvartalsvisSykefraværprosentRepository kvartalsvisSykefraværprosentRepository;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final SektorMappingService sektorMappingService;

    public SykefraværprosentHistorikkService(
            KvartalsvisSykefraværprosentRepository kvartalsvisSykefraværprosentRepository,
            EnhetsregisteretClient enhetsregisteretClient,
            SektorMappingService sektorMappingService) {
        this.kvartalsvisSykefraværprosentRepository = kvartalsvisSykefraværprosentRepository;
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.sektorMappingService = sektorMappingService;
    }


    public List<KvartalsvisSykefraværprosentHistorikk> hentKvartalsvisSykefraværprosentHistorikk(Orgnr orgnr) {
        List<KvartalsvisSykefraværprosentHistorikk> kvartalsvisSykefraværprosentListe = new ArrayList<>();

        kvartalsvisSykefraværprosentListe.add(hentKvartalsvisSykefraværprosentHistorikkLand());

        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        Enhet enhet = enhetsregisteretClient.hentInformasjonOmEnhet(underenhet.getOverordnetEnhetOrgnr());
        Sektor ssbSektor = sektorMappingService.mapTilSSBSektorKode(enhet.getInstitusjonellSektorkode());

        kvartalsvisSykefraværprosentListe.add(hentKvartalsvisSykefraværprosentHistorikkSektor(ssbSektor));
        return kvartalsvisSykefraværprosentListe;
    }


    protected KvartalsvisSykefraværprosentHistorikk hentKvartalsvisSykefraværprosentHistorikkLand() {
        KvartalsvisSykefraværprosentHistorikk kvartalsvisSykefraværprosentHistorikk = new KvartalsvisSykefraværprosentHistorikk();

        kvartalsvisSykefraværprosentHistorikk.setSykefraværsstatistikkType(SykefraværsstatistikkType.LAND);
        kvartalsvisSykefraværprosentHistorikk.setLabel("Norge");
        kvartalsvisSykefraværprosentHistorikk.setKvartalsvisSykefraværProsent(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand("Norge"));

        return kvartalsvisSykefraværprosentHistorikk;
    }

    protected KvartalsvisSykefraværprosentHistorikk hentKvartalsvisSykefraværprosentHistorikkSektor(Sektor ssbSektor) {

        KvartalsvisSykefraværprosentHistorikk kvartalsvisSykefraværprosentHistorikk = new KvartalsvisSykefraværprosentHistorikk();
        kvartalsvisSykefraværprosentHistorikk.setSykefraværsstatistikkType(SykefraværsstatistikkType.SEKTOR);
        kvartalsvisSykefraværprosentHistorikk.setLabel(ssbSektor.getNavn());
        kvartalsvisSykefraværprosentHistorikk.setKvartalsvisSykefraværProsent(
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(ssbSektor));

        return kvartalsvisSykefraværprosentHistorikk;
    }
}
