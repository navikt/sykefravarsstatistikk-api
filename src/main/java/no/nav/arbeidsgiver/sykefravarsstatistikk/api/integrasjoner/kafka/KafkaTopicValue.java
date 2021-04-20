package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.VirksomhetSykefravær;

import java.util.List;

public class KafkaTopicValue {
    VirksomhetSykefravær virksomhetSykefravær;
    List<SykefraværMedKategori> næring5SifferSykefravær;
    SykefraværMedKategori næringSykefravær;
    SykefraværMedKategori sektorSykefravær;
    SykefraværMedKategori landSykefravær;

    public KafkaTopicValue(
            VirksomhetSykefravær virksomhetSykefravær,
            List<SykefraværMedKategori> næring5SifferSykefravær,
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
        if ((virksomhetSykefravær == null && that.virksomhetSykefravær != null)
                || (that.virksomhetSykefravær == null && virksomhetSykefravær != null))
            return false;
        if ((næring5SifferSykefravær == null && that.næring5SifferSykefravær != null)
                || (that.næring5SifferSykefravær == null && næring5SifferSykefravær != null))
            return false;
        if ((næringSykefravær == null && that.næringSykefravær != null)
                || (that.næringSykefravær == null && næringSykefravær != null)) return false;
        if ((sektorSykefravær == null && that.sektorSykefravær != null)
                || (that.sektorSykefravær == null && sektorSykefravær != null)) return false;
        if ((landSykefravær == null && that.landSykefravær != null)
                || (that.landSykefravær == null && landSykefravær != null)) return false;
      /*  if (!virksomhetSykefravær.getOrgnr().equals(that.virksomhetSykefravær.getOrgnr())) return false;
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
            return false;*/

        //if (!virksomhetSykefravær.get().equals(that.virksomhetSykefravær.getÅrstallOgKvartal())) return false;
        //TODO fjerne unødvendige ekstra sjekk
        // jeg vet ikke hvorfor IntelliJ mener at vi kan fjerne that... de er 2 forskjellige objekter, mener jeg.
        return ((virksomhetSykefravær == null && that.virksomhetSykefravær == null) || virksomhetSykefravær.equals(that.virksomhetSykefravær))
                && ((næring5SifferSykefravær == null && that.næring5SifferSykefravær == null) || næring5SifferSykefravær.equals(that.næring5SifferSykefravær))
                && ((næringSykefravær == null && that.næringSykefravær == null) || næringSykefravær.equals(that.næringSykefravær))
                && ((sektorSykefravær == null && that.sektorSykefravær == null) || sektorSykefravær.equals(that.sektorSykefravær))
                && ((landSykefravær == null && that.landSykefravær == null) || landSykefravær.equals(that.landSykefravær));
    }
}
