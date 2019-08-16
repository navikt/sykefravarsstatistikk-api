package no.nav.tag.sykefravarsstatistikk.api.domain.stats.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import no.nav.tag.sykefravarsstatistikk.api.domain.stats.LandStatistikk;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class LandStatistikkJsonSerializer extends JsonSerializer<LandStatistikk> {


    @Override
    public void serialize(LandStatistikk landStatistikk, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("arstall",landStatistikk.getArstall());
        jsonGenerator.writeNumberField("kvartal",landStatistikk.getKvartal());
        jsonGenerator.writeNumberField("mulige_dagsverk",landStatistikk.getMuligeDagsverk());
        jsonGenerator.writeNumberField("tapte_dagsverk",landStatistikk.getTapteDagsverk());
        jsonGenerator.writeNumberField("sykefravar_prosent",landStatistikk.beregnSykkefravarProsent());
        jsonGenerator.writeEndObject();
    }
}
