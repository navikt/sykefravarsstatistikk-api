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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaTopicValue that = (KafkaTopicValue) o;

        if (!virksomhetSykefravær.getOrgnr().equals(that.virksomhetSykefravær.getOrgnr())) return false;
        if (
                (!virksomhetSykefravær.getÅrstallOgKvartal().equals(that.virksomhetSykefravær.getÅrstallOgKvartal()))
                ||(!virksomhetSykefravær.getProsent().equals(that.virksomhetSykefravær.getProsent()))
                ||(!næringSykefravær.getProsent().equals(that.næringSykefravær.getProsent()))
                ||(!sektorSykefravær.getProsent().equals(that.sektorSykefravær.getProsent()))
                ||(!landSykefravær.getProsent().equals(that.landSykefravær.getProsent()))
               // ||(!virksomhetSykefravær.getTapteDagsverk().equals(that.virksomhetSykefravær.getTapteDagsverk()))

                        //TODO sjekk om vi bør teste næring eller nei
                        // (!næring5SifferSykefravær.getProsent().equals(that.næring5SifferSykefravær.getProsent()))
        )
            return false;
        //if (!virksomhetSykefravær.get().equals(that.virksomhetSykefravær.getÅrstallOgKvartal())) return false;
        return (
                landSykefravær.getÅrstall()==that.landSykefravær.getÅrstall()
                &&
                landSykefravær.getKvartal()==that.landSykefravær.getKvartal()
        );
    }
}
