package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles;

import java.util.HashMap;
import java.util.Map;

public enum Sykefraværsvarighet {
    _1_DAG_TIL_7_DAGER("A"),
    _8_DAGER_TIL_16_DAGER("B"),
    _17_DAGER_TIL_8_UKER("C"),
    _8_UKER_TIL_20_UKER("D"),
    _20_UKER_TIL_39_UKER("E"),
    MER_ENN_39_UKER("F"),
    TOTAL("X"),
    UKJENT(null);

    public final String kode;

    private static final Map<String, Sykefraværsvarighet> FRA_KODE = new HashMap<>();

    static {
        for (Sykefraværsvarighet varighet : values()) {
            FRA_KODE.put(varighet.kode, varighet);
        }
    }

    Sykefraværsvarighet(String kode) {
        this.kode = kode;
    }

    public static Sykefraværsvarighet fraKode(String kode) {
        if (FRA_KODE.containsKey(kode)) {
            return FRA_KODE.get(kode);
        } else {
            throw new IllegalArgumentException("Det finnes ingen sykefraværsvarighet med kode " + kode);
        }
    }

    public boolean erKorttidVarighet(){
        switch (this.kode){
            case "A":
            case "B": return true;
            default: return false;
        }
    }
    public boolean erLangtidVarighet(){
        switch (this.kode){
            case "C":
            case "D":
            case "E":
            case "F": return true;
            default: return false;
        }
    }

    @Override
    public String toString() {
        return kode;
    }

}
