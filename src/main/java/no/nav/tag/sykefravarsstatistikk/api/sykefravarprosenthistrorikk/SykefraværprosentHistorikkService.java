package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransjeprogram;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.KlassifikasjonerRepository;
import no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner.SektorMappingService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SykefraværprosentHistorikkService {
    private final KvartalsvisSykefraværprosentRepository kvartalsvisSykefraværprosentRepository;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final SektorMappingService sektorMappingService;
    private final KlassifikasjonerRepository klassifikasjonerRepository;
    private final Bransjeprogram bransjeprogram;

    public SykefraværprosentHistorikkService(
            KvartalsvisSykefraværprosentRepository kvartalsvisSykefraværprosentRepository,
            EnhetsregisteretClient enhetsregisteretClient,
            SektorMappingService sektorMappingService,
            KlassifikasjonerRepository klassifikasjonerRepository,
            Bransjeprogram bransjeprogram) {
        this.kvartalsvisSykefraværprosentRepository = kvartalsvisSykefraværprosentRepository;
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.sektorMappingService = sektorMappingService;
        this.klassifikasjonerRepository = klassifikasjonerRepository;
        this.bransjeprogram = bransjeprogram;
    }


    public List<KvartalsvisSykefraværprosentHistorikk> hentKvartalsvisSykefraværprosentHistorikk(Orgnr orgnr) {
        List<KvartalsvisSykefraværprosentHistorikk> kvartalsvisSykefraværprosentListe = new ArrayList<>();

        kvartalsvisSykefraværprosentListe.add(hentKvartalsvisSykefraværprosentHistorikkLand());

        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        Enhet enhet = enhetsregisteretClient.hentInformasjonOmEnhet(underenhet.getOverordnetEnhetOrgnr());
        Sektor ssbSektor = sektorMappingService.mapTilSSBSektorKode(enhet.getInstitusjonellSektorkode());

        kvartalsvisSykefraværprosentListe.add(hentKvartalsvisSykefraværprosentHistorikkSektor(ssbSektor));

        Optional<Bransje> bransje = bransjeprogram.finnBransje(underenhet);

        if (bransje.isPresent() && bransje.get().lengdePåNæringskoder() == 5) {
            kvartalsvisSykefraværprosentListe.add(hentKvartalsvisSykefraværprosentHistorikkBransje(bransje.get()));
        } else {
            kvartalsvisSykefraværprosentListe.add(hentKvartalsvisSykefraværprosentHistorikkNæring(underenhet));
        }

        kvartalsvisSykefraværprosentListe.add(hentKvartalsvissSykefraværprosentHistorikkVirksomhet(underenhet));

        return kvartalsvisSykefraværprosentListe;
    }


    protected KvartalsvisSykefraværprosentHistorikk hentKvartalsvisSykefraværprosentHistorikkLand() {
        return byggKvartalsvisSykefraværprosentHistorikk(
                SykefraværsstatistikkType.LAND,
                "Norge",
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand("Norge")
        );
    }

    protected KvartalsvisSykefraværprosentHistorikk hentKvartalsvisSykefraværprosentHistorikkSektor(Sektor ssbSektor) {
        return byggKvartalsvisSykefraværprosentHistorikk(
                SykefraværsstatistikkType.SEKTOR,
                ssbSektor.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(ssbSektor)
        );
    }

    protected KvartalsvisSykefraværprosentHistorikk hentKvartalsvisSykefraværprosentHistorikkNæring(Underenhet underenhet) {
        Næringskode5Siffer næring5siffer = underenhet.getNæringskode();
        Næring næring = klassifikasjonerRepository.hentNæring(næring5siffer.hentNæringskode2Siffer());

        return byggKvartalsvisSykefraværprosentHistorikk(
                SykefraværsstatistikkType.NÆRING,
                næring.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentNæring(næring)
        );
    }

    protected KvartalsvisSykefraværprosentHistorikk hentKvartalsvisSykefraværprosentHistorikkBransje(Bransje bransje) {
        return byggKvartalsvisSykefraværprosentHistorikk(
                SykefraværsstatistikkType.BRANSJE,
                bransje.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentBransje(bransje)
        );
    }

    protected KvartalsvisSykefraværprosentHistorikk hentKvartalsvissSykefraværprosentHistorikkVirksomhet(Underenhet underenhet) {
        return byggKvartalsvisSykefraværprosentHistorikk(
                SykefraværsstatistikkType.VIRKSOMHET,
                underenhet.getNavn(),
                kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(underenhet)
        );
    }


    private static KvartalsvisSykefraværprosentHistorikk byggKvartalsvisSykefraværprosentHistorikk(
            SykefraværsstatistikkType sykefraværsstatistikkType,
            String label,
            List<KvartalsvisSykefraværprosent> kvartalsvisSykefraværProsent
    ) {
        KvartalsvisSykefraværprosentHistorikk kvartalsvisSykefraværprosentHistorikk = new KvartalsvisSykefraværprosentHistorikk();
        kvartalsvisSykefraværprosentHistorikk.setSykefraværsstatistikkType(sykefraværsstatistikkType);
        kvartalsvisSykefraværprosentHistorikk.setLabel(label);
        kvartalsvisSykefraværprosentHistorikk.setKvartalsvisSykefraværProsent(kvartalsvisSykefraværProsent);

        return kvartalsvisSykefraværprosentHistorikk;
    }
}
