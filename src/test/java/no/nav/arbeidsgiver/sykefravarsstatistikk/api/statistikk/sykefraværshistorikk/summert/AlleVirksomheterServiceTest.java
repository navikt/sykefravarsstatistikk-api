package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartalMedOrgNr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ActiveProfiles("db-test")
@DataJdbcTest
public class AlleVirksomheterServiceTest {

	@Mock
	private AlleVirksomheterRepository alleVirksomheterRepository;

	private AlleVirksomheterService alleVirksomheterService;

	@BeforeEach
	void setUp() {
		alleVirksomheterService = new AlleVirksomheterService(alleVirksomheterRepository);
	}

	@Test
	public void test(){
		when(alleVirksomheterRepository.hentSykefraværprosentAlleVirksomheterForEttKvartal(any())).thenReturn(
				List.of(new SykefraværForEttKvartalMedOrgNr(
						new ÅrstallOgKvartal(2000, 2),
						"999999999",
						new BigDecimal(4),
						new BigDecimal(4),
						10
				))
		);

		assertTrue(
				alleVirksomheterService.hentAlleVirksomheterForKvartal(new ÅrstallOgKvartal(2000, 2)).size() == 1
		);
	}
}
