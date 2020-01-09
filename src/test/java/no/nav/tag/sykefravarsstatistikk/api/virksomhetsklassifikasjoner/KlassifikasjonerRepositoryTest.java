package no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class KlassifikasjonerRepositoryTest {
    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private KlassifikasjonerRepository klassifikasjonerRepository;

    @Before
    public void setUp() {
        klassifikasjonerRepository = new KlassifikasjonerRepository(namedParameterJdbcTemplate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hentNæringsgruppering__skal_ikke_godta_koder_som_ikke_har_lengde_5() {
        klassifikasjonerRepository.hentNæringsgruppering("1234");
    }

    @Test(expected = IllegalArgumentException.class)
    public void hentNæringsgrupperinger__skal_ikke_godta_koder_som_ikke_har_lengde_2() {
        klassifikasjonerRepository.hentNæringsgruppering("123");
    }
}
