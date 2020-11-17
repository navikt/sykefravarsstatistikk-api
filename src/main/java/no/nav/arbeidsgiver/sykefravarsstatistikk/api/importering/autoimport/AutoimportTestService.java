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

@Component
public class AutoimportTestService {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final BigDecimal BIG_DECIMAL_FEILMARGIN = new BigDecimal("0.01");

    public AutoimportTestService(
            @Qualifier("sykefravarsstatistikkJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<String> testNæringMedVarighetMotNæringstabell() {
        List<String> resultatlinjer = new ArrayList<>();

        List<Rådata> rådataNæring = hentRådataForNæring();
        List<Rådata> rådataNæringMedVarighet = hentRådataForNæringMedVarighet();

        resultatlinjer.add("Antall linjer næring: " + rådataNæring.size());
        resultatlinjer.add("Antall linjer næring med varighet: " + rådataNæringMedVarighet.size());

        List<ÅrstallOgKvartal> årstallOgKvartal = rådataNæring
                .stream()
                .map(Rådata::getÅrstallOgKvartal)
                .collect(Collectors.toList());

        årstallOgKvartal.forEach(kvartal -> {
            resultatlinjer.add("");
            resultatlinjer.add("For årstall=" + kvartal.getÅrstall() + ", kvartal=" + kvartal.getKvartal());

            List<Rådata> dataNæring = getRådataForKvartal(rådataNæring, kvartal);
            List<Rådata> dataNæringMedVarighet = getRådataForKvartal(rådataNæringMedVarighet, kvartal);

            List<Boolean> resultat = rådataErLike(dataNæring, dataNæringMedVarighet);

            resultatlinjer.add("Antall match: " + count(resultat, true));
            resultatlinjer.add("Antall mismatch: " + count(resultat, false));
        });

        return resultatlinjer;
    }

    private List<Boolean> rådataErLike(List<Rådata> rådataNæring, List<Rådata> rådataNæringMedVarighet) {
        List<Boolean> resultat = new ArrayList<>();
        for (int i = 0; i < rådataNæring.size(); i++) {
            Rådata dataNæring = rådataNæring.get(i);
            Optional<Rådata> dataNæringMedVarighet = finnTilsvarendeRådata(dataNæring, rådataNæringMedVarighet);

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

    private Optional<Rådata> finnTilsvarendeRådata(Rådata rådata, List<Rådata> rådataList) {
        return rådataList.stream()
                .filter(rådataListElement -> erLikeUtenomTall(rådataListElement, rådata))
                .findAny();
    }

    public List<String> testNæringMedVarighetMotNæringstabellOld() {
        List<String> resultatlinjer = new ArrayList<>();


        List<Rådata> rådataNæring = hentRådataForNæring();
        List<Rådata> rådataNæringMedVarighet = hentRådataForNæringMedVarighet();

        resultatlinjer.add("Antall linjer næring: " + rådataNæring.size());
        resultatlinjer.add("Antall linjer næring med varighet: " + rådataNæringMedVarighet.size());

        List<ÅrstallOgKvartal> årstallOgKvartal = rådataNæring
                .stream()
                .map(Rådata::getÅrstallOgKvartal)
                .collect(Collectors.toList());

        årstallOgKvartal.forEach(kvartal -> {
            String resultatlinje = kvartal.toString() + ":";

            List<Rådata> dataNæring = getRådataForKvartal(rådataNæring, kvartal);
            List<Rådata> dataNæringMedVarighet = getRådataForKvartal(rådataNæringMedVarighet, kvartal);

            resultatlinje += " Antall rader næring: " + dataNæring.size();
            resultatlinje += " Antall rader næring m varighet: " + dataNæringMedVarighet.size();

            int antallForskjelligeRader = 0;
            //dataNæring

            resultatlinjer.add(resultatlinje);
        });

        return resultatlinjer;
    }

    private String rådataSammenligningResultat(Rådata dataNæring, Rådata dataNæringMedVarighet) {
        String resultat = "AVVIK - ";

        if (!dataNæring.getÅrstallOgKvartal().equals(dataNæringMedVarighet.getÅrstallOgKvartal())) {
            resultat += "årstall: " + dataNæring.getÅrstallOgKvartal().toString() + ", " + dataNæringMedVarighet.toString() + "\n";
        }
        if (!dataNæring.getNæringskode().equals(dataNæringMedVarighet.getNæringskode())) {
            resultat += "årstall: " + dataNæring.getÅrstallOgKvartal().toString() + ", " + dataNæringMedVarighet.toString() + "\n";
        }
        /*
        *
        private final String næringskode;
        private ÅrstallOgKvartal årstallOgKvartal;
        private final int antallPersoner;
        private final BigDecimal tapteDagsverk;
        private final BigDecimal muligeDagsverk;*/
        return resultat;
    }

    private boolean erLike(Rådata data1, Rådata data2) {
        return data1.getNæringskode().equals(data2.getNæringskode()) &&
                data1.getÅrstallOgKvartal().equals(data2.getÅrstallOgKvartal()) &&
                data1.getAntallPersoner().equals(data2.getAntallPersoner()) &&
                bigDecimalApproxEquals(data1.getTapteDagsverk(), data2.getTapteDagsverk()) &&
                bigDecimalApproxEquals(data1.getMuligeDagsverk(), data2.getMuligeDagsverk());
    }

    private boolean erLikeUtenomTall(Rådata data1, Rådata data2) {
        return data1.getNæringskode().equals(data2.getNæringskode()) &&
                data1.getÅrstallOgKvartal().equals(data2.getÅrstallOgKvartal());
    }

    private boolean bigDecimalApproxEquals(BigDecimal number1, BigDecimal number2) {
        return number1.subtract(number2).abs().compareTo(BIG_DECIMAL_FEILMARGIN) < 0;
    }

    private List<Rådata> getRådataForKvartal(List<Rådata> rådata, ÅrstallOgKvartal kvartal) {
        return rådata.stream()
                .filter(data -> data.getÅrstallOgKvartal().equals(kvartal))
                .collect(Collectors.toList());
    }

    private List<Rådata> hentRådataForNæring() {
        return namedParameterJdbcTemplate.query(
                "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk from sykefravar_statistikk_naring5siffer order by arstall, kvartal, naring_kode",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new Rådata(
                        rs.getString("naring_kode"),
                        new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                        rs.getInt("antall_personer"),
                        rs.getBigDecimal("tapte_dagsverk"),
                        rs.getBigDecimal("mulige_dagsverk")
                )
        );
    }

    private List<Rådata> hentRådataForNæringMedVarighet() {
        return namedParameterJdbcTemplate.query(
                "select naring_kode, arstall, kvartal, sum(antall_personer) as sum_antall_personer, sum(tapte_dagsverk) as sum_tapte_dagsverk, sum(mulige_dagsverk) as sum_mulige_dagsverk from SYKEFRAVAR_STATISTIKK_NARING_MED_VARIGHET group by naring_kode, arstall, kvartal order by arstall, kvartal, naring_kode",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new Rådata(
                        rs.getString("naring_kode"),
                        new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                        rs.getInt("sum_antall_personer"),
                        rs.getBigDecimal("sum_tapte_dagsverk"),
                        rs.getBigDecimal("sum_mulige_dagsverk")
                )
        );
    }

    @Value
    @AllArgsConstructor
    private static class Rådata {
        private final String næringskode;
        private ÅrstallOgKvartal årstallOgKvartal;
        private final Integer antallPersoner;
        private final BigDecimal tapteDagsverk;
        private final BigDecimal muligeDagsverk;
    }
}
