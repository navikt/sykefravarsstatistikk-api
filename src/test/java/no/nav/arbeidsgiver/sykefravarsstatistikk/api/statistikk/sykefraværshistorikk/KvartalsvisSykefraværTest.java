package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.sykefravær.KvartalsvisSykefravær;
import org.junit.Test;

import java.math.BigDecimal;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.etÅrstallOgKvartal;
import static org.assertj.core.api.Assertions.assertThat;

public class KvartalsvisSykefraværTest {
    @Test
    public void sykefraværprosent__skal_regne_ut_riktig_prosent_ut_i_fra_tapte_og_mulige_dagsverk() {
        KvartalsvisSykefravær sykefravær = new KvartalsvisSykefravær(
                etÅrstallOgKvartal(),
                new BigDecimal(5),
                new BigDecimal(10),
                20
        );
        assertThat(sykefravær.getProsent()).isEqualTo(new BigDecimal("50.0"));
    }

    @Test
    public void sykefraværprosent__skal_runde_prosenten_opp_ved_tvil() {
        KvartalsvisSykefravær sykefravær = new KvartalsvisSykefravær(
                etÅrstallOgKvartal(),
                new BigDecimal(455),
                new BigDecimal(10000),
                100
        );
        assertThat(sykefravær.getProsent()).isEqualTo(new BigDecimal("4.6"));
    }

    @Test
    public void sykefraværprosent__skal_være_maskert_hvis_antallPersoner_er_4_eller_under() {
        KvartalsvisSykefravær sykefravær = new KvartalsvisSykefravær(
                etÅrstallOgKvartal(),
                new BigDecimal(1),
                new BigDecimal(10),
                4
        );
        assertThat(sykefravær.isErMaskert()).isTrue();
        assertThat(sykefravær.getProsent()).isNull();
        assertThat(sykefravær.getTapteDagsverk()).isNull();
        assertThat(sykefravær.getMuligeDagsverk()).isNull();
    }

    @Test
    public void sykefraværprosent__skal_være_maskert_hvis_antallPersoner_over_4() {
        KvartalsvisSykefravær sykefravær = new KvartalsvisSykefravær(
                etÅrstallOgKvartal(),
                new BigDecimal(1),
                new BigDecimal(10),
                5
        );
        assertThat(sykefravær.isErMaskert()).isFalse();
        assertThat(sykefravær.getProsent()).isNotNull();
        assertThat(sykefravær.getTapteDagsverk()).isNotNull();
        assertThat(sykefravær.getMuligeDagsverk()).isNotNull();
    }

    @Test
    @SneakyThrows
    public void sykefraværprosent__skal_bare_inkludere_relevante_felt_i_json_konvertering() {
        ObjectMapper mapper = new ObjectMapper();

        KvartalsvisSykefravær sykefravær = new KvartalsvisSykefravær(
                etÅrstallOgKvartal(),
                new BigDecimal(5),
                new BigDecimal(10),
                20
        );
        JsonNode json = mapper.readTree(mapper.writeValueAsString(sykefravær));
        JsonNode ønsketJson = mapper.readTree(
                "{" +
                        "    \"årstall\": 2019," +
                        "    \"kvartal\": 4," +
                        "    \"prosent\": 50.0," +
                        "    \"tapteDagsverk\": 5.0," +
                        "    \"muligeDagsverk\": 10.0," +
                        "    \"erMaskert\": false" +
                        "},"
        );

        assertThat(json).isEqualTo(ønsketJson);
    }
}
