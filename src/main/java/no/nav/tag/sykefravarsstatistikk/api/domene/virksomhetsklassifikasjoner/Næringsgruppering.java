package no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner;

import lombok.Value;

/*
TODO Med importering av næringsgrupper vil importering av Næring-objektene være overflødig.
Da kan vi fjerne Virksomhetsklassifikasjon-interfacet.
Muligens kan vi fjerne hele Næring-klassen og bare bruke Næringsgruppering(?)
 */
@Value
public class Næringsgruppering {
    private final String kode5siffer;
    private final String beskrivelse5siffer;
    private final String kode4siffer;
    private final String beskrivelse4siffer;
    private final String kode3siffer;
    private final String beskrivelse3siffer;
    private final String kode2siffer;
    private final String beskrivelse2siffer;

    private final String kodeHovedområde;
    private final String beskrivelseHovedområde;
}
