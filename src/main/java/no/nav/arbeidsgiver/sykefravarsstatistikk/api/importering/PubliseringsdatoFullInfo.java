package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.util.Comparator;

@Getter
@EqualsAndHashCode
public class PubliseringsdatoFullInfo implements Comparable<PubliseringsdatoFullInfo> {

    private final ÅrstallOgKvartal årstallOgKvartal;
    private final String offentligDato;
    private final String aktivitet;
    public PubliseringsdatoFullInfo(
            ÅrstallOgKvartal årstallOgKvartal,
            String offentligDato, // dato for offentliggjøring. todo: kanskje bruke LocalDateTime eller annen datotype her?
            String aktivitet // beskrivelse, typ "Sykefravær pr 3. kvartal 2022"
    ) {
        this.årstallOgKvartal = årstallOgKvartal;
        this.offentligDato = offentligDato;
        this.aktivitet = aktivitet;
    }

    public int getKvartal() {
        return årstallOgKvartal != null ? årstallOgKvartal.getKvartal() : 0;
    }

    public int getÅrstall() {
        return årstallOgKvartal != null ? årstallOgKvartal.getÅrstall() : 0;
    }


    public int compareTo(PubliseringsdatoFullInfo publiseringsdatoFullInfo) {
        return Comparator
                .comparing(PubliseringsdatoFullInfo::getÅrstallOgKvartal)
                .compare(this, publiseringsdatoFullInfo);
    }
}
