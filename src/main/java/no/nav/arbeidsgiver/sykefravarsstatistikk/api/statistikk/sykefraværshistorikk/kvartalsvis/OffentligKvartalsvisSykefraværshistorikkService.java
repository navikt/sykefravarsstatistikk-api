package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikkService.SYKEFRAVÆRPROSENT_LAND_LABEL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikkService.uthentingMedFeilhåndteringOgTimeout;

@Slf4j
@Component
public class OffentligKvartalsvisSykefraværshistorikkService {

    private final KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService;
    private final Bransjeprogram bransjeprogram;

    public OffentligKvartalsvisSykefraværshistorikkService(
            KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService,
            Bransjeprogram bransjeprogram
    ) {
        this.kvartalsvisSykefraværshistorikkService = kvartalsvisSykefraværshistorikkService;
        this.bransjeprogram = bransjeprogram;
    }


    public List<KvartalsvisSykefraværshistorikk> hentSykefraværshistorikkV1Offentlig(
            Underenhet underenhet) {
        Optional<Bransje> bransje =bransjeprogram.finnBransje(underenhet);
        boolean skalHenteDataPåNæring2Siffer =
                bransje.isEmpty()
                        || bransje.get().lengdePåNæringskoder() == 2;

        List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikkListe =
                Stream.of(
                                uthentingMedFeilhåndteringOgTimeout(
                                        () -> kvartalsvisSykefraværshistorikkService
                                                .hentSykefraværshistorikkLand(),
                                        Statistikkategori.LAND,
                                        SYKEFRAVÆRPROSENT_LAND_LABEL),
                                skalHenteDataPåNæring2Siffer ?
                                        kvartalsvisSykefraværshistorikkService
                                                .uthentingAvSykefraværshistorikkNæring(underenhet)
                                        : uthentingMedFeilhåndteringOgTimeout(
                                        () -> kvartalsvisSykefraværshistorikkService
                                                .hentSykefraværshistorikkBransje(bransje.get()),
                                        Statistikkategori.BRANSJE,
                                        bransje.get().getNavn()
                                )
                        )
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());

        return kvartalsvisSykefraværshistorikkListe;

    }
}
