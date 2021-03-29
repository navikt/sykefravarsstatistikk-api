package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartalMedOrgNr;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlleVirksomheterService {
	private final AlleVirksomheterRepository alleVirksomheterRepository;

	public AlleVirksomheterService(AlleVirksomheterRepository alleVirksomheterRepository) {
		this.alleVirksomheterRepository = alleVirksomheterRepository;
	}

	List<SykefraværForEttKvartalMedOrgNr> hentAlleVirksomheterForKvartal(ÅrstallOgKvartal årstallOgKvartal) {
		return alleVirksomheterRepository.hentSykefraværprosentAlleVirksomheterForEttKvartal(årstallOgKvartal);
	}
}
