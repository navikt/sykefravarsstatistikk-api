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
                        || (!virksomhetSykefravær.getProsent().equals(that.virksomhetSykefravær.getProsent()))
                        || (!virksomhetSykefravær.getTapteDagsverk().equals(that.virksomhetSykefravær.getTapteDagsverk()))
                        || (!virksomhetSykefravær.getMuligeDagsverk().equals(that.virksomhetSykefravær.getMuligeDagsverk()))
                        || (!næringSykefravær.getKategori().equals(that.næringSykefravær.getKategori()))
                        || (!næringSykefravær.getProsent().equals(that.næringSykefravær.getProsent()))
                        || (!næringSykefravær.getMuligeDagsverk().equals(that.næringSykefravær.getMuligeDagsverk()))
                        || (!næringSykefravær.getTapteDagsverk().equals(that.næringSykefravær.getTapteDagsverk()))
                        || (!sektorSykefravær.getKategori().equals(that.sektorSykefravær.getKategori()))
                        || (!sektorSykefravær.getProsent().equals(that.sektorSykefravær.getProsent()))
                        || (!sektorSykefravær.getTapteDagsverk().equals(that.sektorSykefravær.getTapteDagsverk()))
                        || (!sektorSykefravær.getMuligeDagsverk().equals(that.sektorSykefravær.getMuligeDagsverk()))
                        || (!landSykefravær.getKategori().equals(that.landSykefravær.getKategori()))
                        || (!landSykefravær.getProsent().equals(that.landSykefravær.getProsent()))
                        || (!landSykefravær.getTapteDagsverk().equals(that.landSykefravær.getTapteDagsverk()))
                        || (!landSykefravær.getMuligeDagsverk().equals(that.landSykefravær.getMuligeDagsverk()))

        )
            return false;

        //if (!virksomhetSykefravær.get().equals(that.virksomhetSykefravær.getÅrstallOgKvartal())) return false;
        return (
                landSykefravær.getÅrstall() == that.landSykefravær.getÅrstall()
                        &&
                        landSykefravær.getKvartal() == that.landSykefravær.getKvartal()
        );
    }
}
