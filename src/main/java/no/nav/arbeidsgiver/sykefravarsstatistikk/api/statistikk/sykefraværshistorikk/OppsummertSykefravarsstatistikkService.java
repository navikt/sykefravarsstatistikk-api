package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.math.BigDecimal.ZERO;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.*;

@Service
public class OppsummertSykefravarsstatistikkService {
    private final SykefraværRepository sykefraværprosentRepository;
    private final BransjeEllerNæringService bransjeEllerNæringService;
    //private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;

    public OppsummertSykefravarsstatistikkService(
            SykefraværRepository sykefraværprosentRepository,
            BransjeEllerNæringService bransjeEllerNæringService,
      //      TilgangskontrollService tilgangskontrollService,
            EnhetsregisteretClient enhetsregisteretClient) {
        this.sykefraværprosentRepository = sykefraværprosentRepository;
        this.bransjeEllerNæringService = bransjeEllerNæringService;
        //this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
    }

    public List<GenerellStatistikk> hentOppsummertStatistikk(String orgnr) {
   /*     InnloggetBruker innloggetBruker =
                tilgangskontrollService.hentInnloggetBrukerForAlleRettigheter();
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                new Orgnr(orgnr), innloggetBruker, "", "");
*/
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(new Orgnr(orgnr));

        BransjeEllerNæring bransjeEllerNæring =
                bransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(
                        underenhet.getNæringskode());
       /* try {
            InnloggetBruker innloggetBrukerMedIARettigheter =
                    tilgangskontrollService.hentInnloggetBruker();
            tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                    new Orgnr(orgnr), innloggetBrukerMedIARettigheter, "", "");

        } catch (TilgangskontrollException tilgangskontrollException) {

        }*/
        return null;
    }

    List<GenerellStatistikk> hentOgBearbeidStatistikk(Underenhet virksomhet) {
        Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværsdata =
                hentUmaskertStatistikkForSisteFemKvartaler(virksomhet);

        GenerellStatistikk sykefraværVirksomhet =
                new GenerellStatistikk(
                        VIRKSOMHET,
                        virksomhet.getNavn(),
                        kalkulerSykefraværSisteFireKvartaler(sykefraværsdata.get(VIRKSOMHET)).toString());

        BransjeEllerNæring næringEllerBransje =
                bransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(
                        virksomhet.getNæringskode());

        GenerellStatistikk sykefraværNæringEllerBransje =
                new GenerellStatistikk(
                        næringEllerBransje.getStatistikkategori(),
                        næringEllerBransje.getVerdiSomString(),
                        kalkulerSykefraværSisteFireKvartaler(
                                sykefraværsdata.get(næringEllerBransje.getStatistikkategori()))
                                .toString());

        GenerellStatistikk sykefraværNorge =
                new GenerellStatistikk(
                        LAND,
                        "Norge",
                        kalkulerSykefraværSisteFireKvartaler(sykefraværsdata.get(LAND)).toString());

        Statistikkategori trendkategoriNæringEllerBransje = næringEllerBransje.isBransje() ? TREND_BRANSJE : TREND_NÆRING;

        GenerellStatistikk trendNæringEllerBransje = new GenerellStatistikk(
                trendkategoriNæringEllerBransje,
                næringEllerBransje.getVerdiSomString(),
                kalkulerTrend(sykefraværsdata.get(næringEllerBransje.getStatistikkategori())));

        return List.of(
                sykefraværVirksomhet,
                sykefraværNæringEllerBransje,
                sykefraværNorge,
                trendNæringEllerBransje
        );
    }

    String kalkulerTrend(List<UmaskertSykefraværForEttKvartal> sykefravær) {
        Optional<UmaskertSykefraværForEttKvartal> sykefraværProsentSisteKvartal =
                sykefravær.stream().filter(
                        umaskertSykefraværForEttKvartal -> umaskertSykefraværForEttKvartal.getÅrstallOgKvartal().equals(SISTE_PUBLISERTE_KVARTAL)
                ).findFirst();
        Optional<UmaskertSykefraværForEttKvartal> sykefraværProsentSisteKvartalEttÅrTilbake =
                sykefravær.stream().filter(
                        umaskertSykefraværForEttKvartal -> umaskertSykefraværForEttKvartal.getÅrstallOgKvartal().equals(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4))
                ).findFirst();
        if (sykefraværProsentSisteKvartal.isEmpty() || sykefraværProsentSisteKvartalEttÅrTilbake.isEmpty()) {
            return "UfullstendigData";// TODO finne bedre retun verdi vi manglende av data grunnlag
        } else {
            return
                    (sykefraværProsentSisteKvartal.get().getProsent().subtract(sykefraværProsentSisteKvartalEttÅrTilbake.get().getProsent())).toString();

        }
    }


    Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>>
    hentUmaskertStatistikkForSisteFemKvartaler(Underenhet bedrift) {
        ÅrstallOgKvartal eldsteÅrstallOgKvartalViBryrOssOm = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4);
        return sykefraværprosentRepository.getAllTheThings(bedrift, eldsteÅrstallOgKvartalViBryrOssOm);
    }

    private BigDecimal kalkulerSykefraværSisteFireKvartaler(
            List<UmaskertSykefraværForEttKvartal> statistikk) {

        return ekstraherSisteFireKvartaler(statistikk).stream()
                .map(
                        data ->
                                new Sykefravær(
                                        data.getMuligeDagsverk(), data.getTapteDagsverk(), data.getAntallPersoner()))
                .reduce(Sykefravær.NULLPUNKT, Sykefravær::leggSammen)
                .kalkulerFraværsprosent();
    }

    private List<UmaskertSykefraværForEttKvartal> ekstraherSisteFireKvartaler(
            List<UmaskertSykefraværForEttKvartal> statistikk) {
        List<ÅrstallOgKvartal> sisteFireKvartaler =
                IntStream.range(0, 4)
                        .mapToObj(SISTE_PUBLISERTE_KVARTAL::minusKvartaler)
                        .collect(Collectors.toList());

        return statistikk.stream()
                .filter(data -> sisteFireKvartaler.contains(data.getÅrstallOgKvartal()))
                .collect(Collectors.toList());
    }

}

class Sykefravær {
    public BigDecimal mulige;
    public BigDecimal tapte;
    public int antallPersonerIGrunnlaget;

    Sykefravær(BigDecimal mulige, BigDecimal tapte, int antallPersonerIGrunnlaget) {
        this.mulige = mulige;
        this.tapte = tapte;
        this.antallPersonerIGrunnlaget = antallPersonerIGrunnlaget;
    }

    BigDecimal kalkulerFraværsprosent() {
        return tapte.divide(mulige, RoundingMode.HALF_UP);
    }

    Sykefravær leggSammen(Sykefravær other) {
        return new Sykefravær(
                this.mulige.add(other.mulige),
                this.tapte.add(other.tapte),
                this.antallPersonerIGrunnlaget + other.antallPersonerIGrunnlaget);
    }

    static Sykefravær NULLPUNKT = new Sykefravær(ZERO, ZERO, 0);
}
