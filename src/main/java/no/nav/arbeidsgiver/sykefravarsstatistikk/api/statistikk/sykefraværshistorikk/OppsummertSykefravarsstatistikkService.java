package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        List<GenerellStatistikk> samletKategoerierListe = new ArrayList<>();

        Optional<Sykefraværsprosent> fraværsprosentVirksomhet = kalkulerSykefraværSisteFireKvartaler(sykefraværsdata.get(VIRKSOMHET));
        fraværsprosentVirksomhet.ifPresent(sykefraværsprosent -> samletKategoerierListe.add(
                new GenerellStatistikk(
                        VIRKSOMHET,
                        virksomhet.getNavn(),
                        sykefraværsprosent.getProsent().toString(),
                        sykefraværsprosent.getKvartalerIBeregningen())));

        BransjeEllerNæring næringEllerBransje =
                bransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(
                        virksomhet.getNæringskode());

        Optional<Sykefraværsprosent> statistikkBransjeEllerNæring = kalkulerSykefraværSisteFireKvartaler(sykefraværsdata.get(næringEllerBransje.getStatistikkategori()));
        statistikkBransjeEllerNæring.ifPresent(sykefraværsprosent -> samletKategoerierListe.add(new GenerellStatistikk(
                næringEllerBransje.getStatistikkategori(),
                næringEllerBransje.getVerdiSomString(),
                sykefraværsprosent.getProsent().toString(),
                sykefraværsprosent.getKvartalerIBeregningen())));
        Optional<Sykefraværsprosent> fraværsprosentNorge = kalkulerSykefraværSisteFireKvartaler(sykefraværsdata.get(LAND));

        fraværsprosentNorge.ifPresent(sykefraværsprosent -> samletKategoerierListe.add(new GenerellStatistikk(
                LAND,
                "Norge",
                sykefraværsprosent.getProsent().toString(), sykefraværsprosent.getKvartalerIBeregningen())));

        Statistikkategori trendkategoriNæringEllerBransje = næringEllerBransje.isBransje() ? TREND_BRANSJE : TREND_NÆRING;

        statistikkBransjeEllerNæring.ifPresent(sykefraværsprosent -> samletKategoerierListe.add(new GenerellStatistikk(
                trendkategoriNæringEllerBransje,
                næringEllerBransje.getVerdiSomString(),
                kalkulerTrend(sykefraværsdata.get(næringEllerBransje.getStatistikkategori())),
                statistikkBransjeEllerNæring.get().getKvartalerIBeregningen())));

        return samletKategoerierListe;
    }

    String kalkulerTrend(List<UmaskertSykefraværForEttKvartal> sykefravær) {

        List<UmaskertSykefraværForEttKvartal> relevanteDatapunkter =
                sykefravær.stream()
                        .filter(
                                sykefraværEttKvartal ->
                                        sykefraværEttKvartal.getÅrstallOgKvartal().equals(SISTE_PUBLISERTE_KVARTAL)
                                                || sykefraværEttKvartal.getÅrstallOgKvartal()
                                                .equals(SISTE_PUBLISERTE_KVARTAL.minusEttÅr()))
                        .sorted(Comparator.comparing(UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal))
                        .collect(Collectors.toList());

        if (relevanteDatapunkter.size() != 2) {
            return "UfullstendigData"; // TODO finne bedre retun verdi vi manglende av data grunnlag
        }

        return relevanteDatapunkter.stream()
                .map(UmaskertSykefraværForEttKvartal::getProsent)
                .reduce((s1, s2) -> s2.subtract(s1))
                .get()
                .toString();
    }

    Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>>
    hentUmaskertStatistikkForSisteFemKvartaler(Underenhet bedrift) {
        ÅrstallOgKvartal eldsteÅrstallOgKvartalViBryrOssOm = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4);
        return sykefraværprosentRepository.hentUmaskertSykefraværAlleKategorier(bedrift, eldsteÅrstallOgKvartalViBryrOssOm);
    }

    private Optional<Sykefraværsprosent> kalkulerSykefraværSisteFireKvartaler(
            List<UmaskertSykefraværForEttKvartal> statistikk) {

        Stream<UmaskertSykefraværForEttKvartal> statistikkStream
                = ekstraherSisteFireKvartaler(statistikk).stream();

        if (statistikkStream.findAny().isEmpty()) return null;

        BigDecimal prosent = statistikkStream.map(
                        data ->
                                new Sykefravær(
                                        data.getMuligeDagsverk(), data.getTapteDagsverk(), data.getAntallPersoner()))
                .reduce(Sykefravær.NULLPUNKT, Sykefravær::leggSammen)
                .kalkulerFraværsprosent();

        List<ÅrstallOgKvartal> kvartaler = statistikkStream.map(
                UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal).collect(Collectors.toList());

        return Optional.of(new Sykefraværsprosent(prosent, kvartaler));
    }


    private List<UmaskertSykefraværForEttKvartal> ekstraherSisteFireKvartaler(
            List<UmaskertSykefraværForEttKvartal> statistikk) {
        List<ÅrstallOgKvartal> sisteFireKvartaler =
                IntStream.range(0, 4)
                        .mapToObj(SISTE_PUBLISERTE_KVARTAL::minusKvartaler)
                        .collect(Collectors.toList());

        if (statistikk != null) {
            return statistikk.stream()
                    .filter(data -> sisteFireKvartaler.contains(data.getÅrstallOgKvartal()))
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

}


@Data
class Sykefraværsprosent {
    private final BigDecimal prosent;
    private final List<ÅrstallOgKvartal> kvartalerIBeregningen;
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
        return tapte.divide(mulige);
    }

    Sykefravær leggSammen(Sykefravær other) {
        return new Sykefravær(
                this.mulige.add(other.mulige),
                this.tapte.add(other.tapte),
                this.antallPersonerIGrunnlaget + other.antallPersonerIGrunnlaget);
    }

    static Sykefravær NULLPUNKT = new Sykefravær(ZERO, ZERO, 0);
}
