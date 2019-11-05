package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@Slf4j
@Component
public class EnhetsregisteretClient {
    private final RestTemplate restTemplate;
    private final String enhetsregisteretUrl;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public EnhetsregisteretClient(
            RestTemplate restTemplate,
            @Value("${enhetsregisteret.url}") String enhetsregisteretUrl
  ) {
        this.restTemplate = restTemplate;
        this.enhetsregisteretUrl = enhetsregisteretUrl;
    }

    public Enhet hentEnhetsinformasjon(Orgnr orgnr) {
        URI uri = UriComponentsBuilder.fromUriString(enhetsregisteretUrl)
                .pathSegment("enheter")
                .queryParam("organisasjonsnummer", orgnr.getVerdi())
                .build()
                .toUri();

        try {
            String respons = restTemplate.getForObject(uri, String.class);

            return mapTilEnhet(respons);
        } catch (RestClientException e) {
            log.error("Feil ved kall til Enhetsregisteret", e);
            throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret", e);
        }
    }

    private Enhet mapTilEnhet(String jsonResponseFraEnhetsregisteret) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponseFraEnhetsregisteret);

            JsonNode enheterJson = jsonNode.get("_embedded").get("enheter");
            if (enheterJson.size() != 1) {
                throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret. Kall returnerte ikke nøyaktig 1 enhet.");
            }

            JsonNode enhetJson = enheterJson.get(0);
            JsonNode næringskodeJson = enhetJson.get("naeringskode1");
            JsonNode sektorJson = enhetJson.get("institusjonellSektorkode");

            Orgnr orgnr = new Orgnr(enhetJson.get("organisasjonsnummer").asText());
            String navn = enhetJson.get("navn").asText();
            Næringskode næringskode = new Næringskode(
                    næringskodeJson.get("kode").asText(),
                    næringskodeJson.get("beskrivelse").asText()
            );
            InstitusjonellSektorkode sektor = new InstitusjonellSektorkode(
                    sektorJson.get("kode").asText(),
                    sektorJson.get("beskrivelse").asText()
            );

            return new Enhet(orgnr, navn, næringskode, sektor);

        } catch (IOException | NullPointerException e) {
            throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.");
        }
    }
}
