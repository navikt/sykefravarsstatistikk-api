package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import lombok.AllArgsConstructor;
import lombok.Value;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;

@Component
public class ImporteringKvalitetssjekkService {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final BigDecimal BIG_DECIMAL_FEILMARGIN = new BigDecimal("0.01");

    public ImporteringKvalitetssjekkService(
            @Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<String> kvalitetssjekkNæringMedVarighetOgMedGraderingMotNæringstabell() {
        List<String> resultatlinjer = new ArrayList<>();

        List<RådataMedNæringskode> rådataForNæring = hentRådataForNæring();
        List<RådataMedNæringskode> rådataForNåringMedVarighet = hentRådataForNæringMedVarighet();
        List<RådataMedNæringskode> rådataForNæringMedGradering = hentRådataForNæringMedGradering(RECTYPE_FOR_VIRKSOMHET);
        List<Rådata> rådataForVirksomhet = hentSykefraværRådataForVirksomhet();
        List<Rådata> rådataForVirksomhetMedGradering = hentSykefraværRådataForVirksomhetMedGradering();

        resultatlinjer.add("Antall linjer næring: " + rådataForNæring.size());
        resultatlinjer.add("Antall linjer næring med varighet: " + rådataForNåringMedVarighet.size());
        resultatlinjer.add("Antall linjer næring med gradering: " + rådataForNæringMedGradering.size());
        resultatlinjer.add("Antall linjer virksomhet: " + rådataForVirksomhet.size());
        resultatlinjer.add("Antall linjer virksomhet med gradering: " + rådataForVirksomhetMedGradering.size());

        List<ÅrstallOgKvartal> årstallOgKvartal = rådataForNæring
                .stream()
                .map(RådataMedNæringskode::getÅrstallOgKvartal)
                .distinct()
                .collect(Collectors.toList());

        årstallOgKvartal.forEach(
                kvartal -> {
                    resultatlinjer.add("");
                    resultatlinjer.add("For årstall=" + kvartal.getÅrstall() + ", kvartal=" + kvartal.getKvartal());

                    List<RådataMedNæringskode> dataNæring = getRådataForKvartal(rådataForNæring, kvartal);
                    List<RådataMedNæringskode> dataNæringMedVarighet = getRådataForKvartal(rådataForNåringMedVarighet, kvartal);
                    List<RådataMedNæringskode> dataNæringMedGradering = getRådataForKvartal(rådataForNæringMedGradering, kvartal);

                    List<Boolean> resultatForNæringMedVarighet = rådataErLike(dataNæring, dataNæringMedVarighet);

                    resultatlinjer.add("Antall match med varighet data: " + count(resultatForNæringMedVarighet, true));
                    resultatlinjer.add("Antall mismatch med varighet data: " + count(resultatForNæringMedVarighet, false));

                    List<Boolean> resultatForNæringMedGradering = rådataErLike(dataNæring, dataNæringMedGradering);

                    resultatlinjer.add("Antall match med gradering data: " + count(resultatForNæringMedGradering, true));
                    resultatlinjer.add("Antall mismatch med gradering data: " + count(resultatForNæringMedGradering, false));

                    Rådata dataForVirksomhetForEttKvartal = getSykefraværRådataForKvartal(rådataForVirksomhet, kvartal);
                    Rådata dataForVirksomhetMedGraderingForEttKvartal = getSykefraværRådataForKvartal(rådataForVirksomhetMedGradering, kvartal);

                    boolean erDataForVirksomhetOgDataForVirksomhetMedGraderingLike = sykefraværRådataErLike(
                            dataForVirksomhetForEttKvartal,
                            dataForVirksomhetMedGraderingForEttKvartal
                    );

                    resultatlinjer.add(
                            "Antall match med gradering for virksomhet: "
                                    + (erDataForVirksomhetOgDataForVirksomhetMedGraderingLike ? "1" : "0")
                    );
                    resultatlinjer.add(
                            "Antall mismatch med gradering for virksomhet: "
                                    + (erDataForVirksomhetOgDataForVirksomhetMedGraderingLike ? "0" : "1")
                    );
                });


        return resultatlinjer;
    }


    protected static boolean sykefraværRådataErLike(
            Rådata sykefraværRådata,
            Rådata sykefraværRådataMedGradering
    ) {
        return (sykefraværRådata == null || sykefraværRådataMedGradering == null) ?
                (sykefraværRådata == null && sykefraværRådataMedGradering == null) :
                erLike(sykefraværRådata, sykefraværRådataMedGradering);
    }

    private List<Boolean> rådataErLike(List<RådataMedNæringskode> sykefraværMedNæringskodeRådataNæring, List<RådataMedNæringskode> sykefraværMedNæringskodeNæringMedVarighetRådata) {
        List<Boolean> resultat = new ArrayList<>();

        for (RådataMedNæringskode dataNæring : sykefraværMedNæringskodeRådataNæring) {
            Optional<RådataMedNæringskode> dataNæringMedVarighet = finnTilsvarendeRådata(dataNæring, sykefraværMedNæringskodeNæringMedVarighetRådata);

            if (dataNæringMedVarighet.isPresent()) {
                resultat.add(erLike(dataNæring, dataNæringMedVarighet.get()));
            } else {
                resultat.add(false);
            }
        }
        return resultat;
    }

    private long count(List<Boolean> list, boolean matchBool) {
        return list.stream().filter(bool -> bool.equals(matchBool)).count();
    }

    private Optional<RådataMedNæringskode> finnTilsvarendeRådata(RådataMedNæringskode sykefraværMedNæringskodeRådata, List<RådataMedNæringskode> sykefraværMedNæringskodeRådataList) {
        return sykefraværMedNæringskodeRådataList.stream()
                .filter(sykefraværMedNæringskodeRådataListElement -> erLikeUtenomTall(sykefraværMedNæringskodeRådataListElement, sykefraværMedNæringskodeRådata))
                .findAny();
    }

    private Optional<Rådata> finnTilsvarendeRådata(
            Rådata rådata,
            List<Rådata> rådataList
    ) {
        return rådataList.stream()
                .filter(rådataListElement -> rådataListElement.årstallOgKvartal.equals(rådata.årstallOgKvartal))
                .findAny();
    }

    private boolean erLike(RådataMedNæringskode data1, RådataMedNæringskode data2) {
        return data1.getNæringskode().equals(data2.getNæringskode()) &&
                erLike(
                        new Rådata(
                                data1.getÅrstallOgKvartal(),
                                data1.getAntallPersoner(),
                                data1.getTapteDagsverk(),
                                data1.getMuligeDagsverk()
                        ),
                        new Rådata(
                                data2.getÅrstallOgKvartal(),
                                data2.getAntallPersoner(),
                                data2.getTapteDagsverk(),
                                data2.getMuligeDagsverk()
                        )
                );
    }

    private static boolean erLike(Rådata data1, Rådata data2) {
        return data1.getÅrstallOgKvartal().equals(data2.getÅrstallOgKvartal()) &&
                data1.getAntallPersoner().equals(data2.getAntallPersoner()) &&
                bigDecimalApproxEquals(data1.getTapteDagsverk(), data2.getTapteDagsverk()) &&
                bigDecimalApproxEquals(data1.getMuligeDagsverk(), data2.getMuligeDagsverk());
    }

    private boolean erLikeUtenomTall(RådataMedNæringskode data1, RådataMedNæringskode data2) {
        return data1.getNæringskode().equals(data2.getNæringskode()) &&
                data1.getÅrstallOgKvartal().equals(data2.getÅrstallOgKvartal());
    }

    private static boolean bigDecimalApproxEquals(BigDecimal number1, BigDecimal number2) {
        return number1.subtract(number2).abs().compareTo(BIG_DECIMAL_FEILMARGIN) < 0;
    }

    private List<RådataMedNæringskode> getRådataForKvartal(List<RådataMedNæringskode> rådata, ÅrstallOgKvartal kvartal) {
        return rådata.stream()
                .filter(data -> data.getÅrstallOgKvartal().equals(kvartal))
                .collect(Collectors.toList());
    }

    private Rådata getSykefraværRådataForKvartal(
            List<Rådata> sykefraværRådata,
            ÅrstallOgKvartal kvartal
    ) {
        return sykefraværRådata.stream()
                .filter(data -> data.getÅrstallOgKvartal().equals(kvartal))
                .findAny()
                .orElse(null);
    }

    private List<RådataMedNæringskode> hentRådataForNæring() {
        return namedParameterJdbcTemplate.query(
                "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk " +
                        "from sykefravar_statistikk_naring5siffer order by arstall, kvartal, naring_kode",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new RådataMedNæringskode(
                        rs.getString("naring_kode"),
                        new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                        rs.getInt("antall_personer"),
                        rs.getBigDecimal("tapte_dagsverk"),
                        rs.getBigDecimal("mulige_dagsverk")
                )
        );
    }

    private List<RådataMedNæringskode> hentRådataForNæringMedVarighet() {
        return namedParameterJdbcTemplate.query(
                "select naring_kode, arstall, kvartal, " +
                        "sum(antall_personer) as sum_antall_personer, " +
                        "sum(tapte_dagsverk) as sum_tapte_dagsverk, " +
                        "sum(mulige_dagsverk) as sum_mulige_dagsverk " +
                        "from SYKEFRAVAR_STATISTIKK_NARING_MED_VARIGHET " +
                        "group by naring_kode, arstall, kvartal " +
                        "order by arstall, kvartal, naring_kode",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new RådataMedNæringskode(
                        rs.getString("naring_kode"),
                        new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                        rs.getInt("sum_antall_personer"),
                        rs.getBigDecimal("sum_tapte_dagsverk"),
                        rs.getBigDecimal("sum_mulige_dagsverk")
                )
        );
    }

    private List<RådataMedNæringskode> hentRådataForNæringMedGradering(String rectype) {
        MapSqlParameterSource parametre = new MapSqlParameterSource()
                .addValue("rectype", rectype);
        return namedParameterJdbcTemplate.query(
                "select naring_kode, arstall, kvartal, " +
                        "sum(antall_personer) as sum_antall_personer, " +
                        "sum(tapte_dagsverk) as sum_tapte_dagsverk, " +
                        "sum(mulige_dagsverk) as sum_mulige_dagsverk " +
                        "from sykefravar_statistikk_virksomhet_med_gradering " +
                        "where rectype = :rectype" +
                        "group by naring_kode, arstall, kvartal " +
                        "order by arstall, kvartal, naring_kode",
                parametre,
                (rs, rowNum) -> new RådataMedNæringskode(
                        rs.getString("naring_kode"),
                        new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                        rs.getInt("sum_antall_personer"),
                        rs.getBigDecimal("sum_tapte_dagsverk"),
                        rs.getBigDecimal("sum_mulige_dagsverk")
                )
        );
    }

    private List<Rådata> hentSykefraværRådataForVirksomhet() {
        return namedParameterJdbcTemplate.query(
                "select  arstall, kvartal, " +
                        "sum(antall_personer) as sum_antall_personer, " +
                        "sum(tapte_dagsverk) as sum_tapte_dagsverk, " +
                        "sum(mulige_dagsverk) as sum_mulige_dagsverk " +
                        "from sykefravar_statistikk_virksomhet " +
                        "group by  arstall, kvartal " +
                        "order by arstall, kvartal ",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new Rådata(
                        new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                        rs.getInt("sum_antall_personer"),
                        rs.getBigDecimal("sum_tapte_dagsverk"),
                        rs.getBigDecimal("sum_mulige_dagsverk")
                )
        );
    }

    private List<Rådata> hentSykefraværRådataForVirksomhetMedGradering() {
        return namedParameterJdbcTemplate.query(
                "select arstall, kvartal, " +
                        "sum(antall_personer) as sum_antall_personer, " +
                        "sum(tapte_dagsverk) as sum_tapte_dagsverk, " +
                        "sum(mulige_dagsverk) as sum_mulige_dagsverk " +
                        "from sykefravar_statistikk_virksomhet_med_gradering " +
                        "group by arstall, kvartal " +
                        "order by arstall, kvartal ",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new Rådata(
                        new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                        rs.getInt("sum_antall_personer"),
                        rs.getBigDecimal("sum_tapte_dagsverk"),
                        rs.getBigDecimal("sum_mulige_dagsverk")
                )
        );
    }

    @AllArgsConstructor
    @Value
    protected static class Rådata {
        private ÅrstallOgKvartal årstallOgKvartal;
        private final Integer antallPersoner;
        private final BigDecimal tapteDagsverk;
        private final BigDecimal muligeDagsverk;
    }

    @AllArgsConstructor
    @Value
    private static class RådataMedNæringskode {
        private final String næringskode;
        private ÅrstallOgKvartal årstallOgKvartal;
        private final Integer antallPersoner;
        private final BigDecimal tapteDagsverk;
        private final BigDecimal muligeDagsverk;
    }
}
