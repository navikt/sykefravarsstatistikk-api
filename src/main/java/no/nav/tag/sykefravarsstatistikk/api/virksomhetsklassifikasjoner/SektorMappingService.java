package no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner;

import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.InstitusjonellSektorkode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class SektorMappingService {
    private static final Map<String, String> mapFraSSBSektorKodeTilInstitusjonellSektorkode;
    static {
        Map<String, String> map = new HashMap<>() {{
            put("1110", "3");
            put("1120", "3");
            put("1510", "3");
            put("1520", "3");
            put("2100", "3");
            put("2300", "3");
            put("2500", "3");
            put("3100", "1");
            put("3200", "3");
            put("3500", "3");
            put("3600", "3");
            put("3900", "1");
            put("4100", "3");
            put("4300", "3");
            put("4500", "3");
            put("4900", "3");
            put("5500", "3");
            put("5700", "3");
            put("6100", "1");
            put("6500", "2");
            put("7000", "3");
            put("8200", "3");
            put("8300", "3");
            put("8500", "3");
            put("9000", "3");
        }};

        mapFraSSBSektorKodeTilInstitusjonellSektorkode = Collections.unmodifiableMap(map);
    }

    public Sektor mapTilSSBSektorKode(InstitusjonellSektorkode sektor) {
        String kode = mapFraSSBSektorKodeTilInstitusjonellSektorkode.get(sektor.getKode());

        return new Sektor(
                kode,
                hentLabel(kode)
        );
    }

    private String hentLabel(String kode) {
        switch (kode) {
            case "1": return "Statlig forvaltning";
            case "2": return "Kommunal forvaltning";
            case "3": return "Privat og offentlig næringsvirksomhet";
            default: return "Ukjent";

        }
    }
}

