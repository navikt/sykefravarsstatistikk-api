package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.enUnderenhet;
import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.enUnderenhetBuilder;
import static no.nav.tag.sykefravarsstatistikk.api.sammenligning.SammenligningRepository.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SykefraværprosentRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Captor
    private ArgumentCaptor<SqlParameterSource> parameterArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> sqlArgumentCaptor;

    private SammenligningRepository repository;

    @Before
    public void setUp() {
        repository = new SammenligningRepository(jdbcTemplate);
    }

    @Test
    public void hentSykefraværprosentLand_skal_sende_med_og_bruke_riktige_parametre() {
        repository.hentSykefraværprosentLand(2019, 1);
        captureArgumenterTilJdbcTemplate();

        SqlParameterSource parametre = parameterArgumentCaptor.getValue();
        String sql = sqlArgumentCaptor.getValue();

        assertThat(parametre.getValue(ÅRSTALL)).isEqualTo(2019);
        assertThat(parametre.getValue(KVARTAL)).isEqualTo(1);
        assertThat(parametre.getParameterNames().length).isEqualTo(2);

        assertThat(sql).contains(":" + ÅRSTALL);
        assertThat(sql).contains(":" + KVARTAL);
    }

    @Test
    public void hentSykefraværprosentSektor_skal_sende_med_og_bruke_riktige_parametre() {
        repository.hentSykefraværprosentSektor(2018, 2, "0");
        captureArgumenterTilJdbcTemplate();

        SqlParameterSource parametre = parameterArgumentCaptor.getValue();
        String sql = sqlArgumentCaptor.getValue();

        assertThat(parametre.getValue(ÅRSTALL)).isEqualTo(2018);
        assertThat(parametre.getValue(KVARTAL)).isEqualTo(2);
        assertThat(parametre.getValue(SEKTOR)).isEqualTo("0");
        assertThat(parametre.getParameterNames().length).isEqualTo(3);

        assertThat(sql).contains(":" + ÅRSTALL);
        assertThat(sql).contains(":" + KVARTAL);
        assertThat(sql).contains(":" + SEKTOR);
    }

    @Test
    public void hentSykefraværprosentNæring_skal_sende_med_og_bruke_riktige_parametre() {
        repository.hentSykefraværprosentNæring(2017, 3, new Næringskode5Siffer("51253", ""));
        captureArgumenterTilJdbcTemplate();

        SqlParameterSource parametre = parameterArgumentCaptor.getValue();
        String sql = sqlArgumentCaptor.getValue();

        assertThat(parametre.getValue(ÅRSTALL)).isEqualTo(2017);
        assertThat(parametre.getValue(KVARTAL)).isEqualTo(3);
        assertThat(parametre.getValue(NÆRING)).isEqualTo("51");
        assertThat(parametre.getParameterNames().length).isEqualTo(3);

        assertThat(sql).contains(":" + ÅRSTALL);
        assertThat(sql).contains(":" + KVARTAL);
        assertThat(sql).contains(":" + NÆRING);
    }

    @Test
    public void hentSykefraværprosentVirksomhet_skal_sende_med_og_bruke_riktige_parametre() {
        repository.hentSykefraværprosentVirksomhet(
                2013,
                4,
                enUnderenhet("123456789")
        );
        captureArgumenterTilJdbcTemplate();

        SqlParameterSource parametre = parameterArgumentCaptor.getValue();
        String sql = sqlArgumentCaptor.getValue();

        assertThat(parametre.getValue(ÅRSTALL)).isEqualTo(2013);
        assertThat(parametre.getValue(KVARTAL)).isEqualTo(4);
        assertThat(parametre.getValue(ORGNR)).isEqualTo("123456789");
        assertThat(parametre.getParameterNames().length).isEqualTo(3);

        assertThat(sql).contains(":" + ÅRSTALL);
        assertThat(sql).contains(":" + KVARTAL);
        assertThat(sql).contains(":" + ORGNR);
    }

    private void captureArgumenterTilJdbcTemplate() {
        verify(jdbcTemplate).queryForObject(
                sqlArgumentCaptor.capture(),
                parameterArgumentCaptor.capture(),
                any(RowMapper.class)
        );
    }

}