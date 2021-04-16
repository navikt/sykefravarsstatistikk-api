package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;

public class KafkaTopicValue {
    VirksomhetSykefravær virksomhetSykefravær;
    SykefraværMedKategori næring5SifferSykefravær;
    SykefraværMedKategori næringSykefravær;
    SykefraværMedKategori sektorSykefravær;
    SykefraværMedKategori landSykefravær;

    public KafkaTopicValue(
            VirksomhetSykefravær virksomhetSykefravær,
            SykefraværMedKategori næring5SifferSykefravær,
            SykefraværMedKategori næringSykefravær,
            SykefraværMedKategori sektorSykefravær,
            SykefraværMedKategori landSykefravær
    ) {
        this.virksomhetSykefravær = virksomhetSykefravær;
        this.næring5SifferSykefravær = næring5SifferSykefravær;
        this.næringSykefravær = næringSykefravær;
        this.sektorSykefravær = sektorSykefravær;
        this.landSykefravær = landSykefravær;
    }
}
