package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.sammenligning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class SykefraværprosentTest {
    @Test
    public void sykefraværprosent__skal_regne_ut_riktig_prosent_ut_i_fra_tapte_og_mulige_dagsverk() {
        Sykefraværprosent sykefraværprosent = new Sykefraværprosent("", new BigDecimal(5), new BigDecimal(10), 20);
        assertThat(sykefraværprosent.getProsent()).isEqualTo(new BigDecimal("50.0"));
    }

    @Test
    public void sykefraværprosent__skal_runde_prosenten_opp_ved_tvil() {
        Sykefraværprosent sykefraværprosent = new Sykefraværprosent("", new BigDecimal(455), new BigDecimal(10000), 100);
        assertThat(sykefraværprosent.getProsent()).isEqualTo(new BigDecimal("4.6"));
    }

    @Test
    public void sykefraværprosent__skal_være_maskert_hvis_antallPersoner_er_4_eller_under() {
        Sykefraværprosent sykefraværprosent = new Sykefraværprosent("", new BigDecimal(1), new BigDecimal(10), 4);
        assertThat(sykefraværprosent.isErMaskert()).isTrue();
        assertThat(sykefraværprosent.getAntallPersoner()).isNull();
        assertThat(sykefraværprosent.getProsent()).isNull();
        assertThat(sykefraværprosent.getTapteDagsverk()).isNull();
        assertThat(sykefraværprosent.getMuligeDagsverk()).isNull();
    }

    @Test
    public void sykefraværprosent__skal_være_maskert_hvis_antallPersoner_over_4() {
        Sykefraværprosent sykefraværprosent = new Sykefraværprosent("", new BigDecimal(1), new BigDecimal(10), 5);
        assertThat(sykefraværprosent.isErMaskert()).isFalse();
        assertThat(sykefraværprosent.getAntallPersoner()).isNotNull();
        assertThat(sykefraværprosent.getProsent()).isNotNull();
        assertThat(sykefraværprosent.getTapteDagsverk()).isNotNull();
        assertThat(sykefraværprosent.getMuligeDagsverk()).isNotNull();
    }

    @Test
    @SneakyThrows
    public void sykefraværprosent__skal_bare_inkludere_relevante_felt_i_json_konvertering() {
        ObjectMapper mapper = new ObjectMapper();

        Sykefraværprosent sykefraværprosent = new Sykefraværprosent("Navn AS", new BigDecimal(5), new BigDecimal(10), 20);
        JsonNode json = mapper.readTree(mapper.writeValueAsString(sykefraværprosent));
        JsonNode ønsketJson = mapper.readTree(
                "{" +
                        "    \"label\": \"Navn AS\"," +
                        "    \"prosent\": 50.0," +
                        "    \"erMaskert\": false" +
                        "},"
        );

        assertThat(json).isEqualTo(ønsketJson);
    }
}
