package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

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

    public Underenhet hentInformasjonOmUnderenhet(Orgnr orgnrTilUnderenhet) {
        String url = enhetsregisteretUrl + "enheter/" + orgnrTilUnderenhet.getVerdi();

        try {
            String respons = restTemplate.getForObject(url, String.class);
            return mapTilUnderenhet(respons);
        } catch (RestClientException e) {
            throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret", e);
        }
    }

    private Underenhet mapTilUnderenhet(String jsonResponseFraEnhetsregisteret) {
        try {
            JsonNode enhetJson = objectMapper.readTree(jsonResponseFraEnhetsregisteret);
            JsonNode næringskodeJson = enhetJson.get("naeringskode1");
            JsonNode sektorJson = enhetJson.get("institusjonellSektorkode");

            Orgnr orgnr = new Orgnr(enhetJson.get("organisasjonsnummer").asText());
            Orgnr overordnetEnhetOrgnr = new Orgnr(enhetJson.get("overordnetEnhet").asText());
            String navn = enhetJson.get("navn").asText();
            Næringskode næringskode = new Næringskode(
                    næringskodeJson.get("kode").asText(),
                    næringskodeJson.get("beskrivelse").asText()
            );
            InstitusjonellSektorkode sektor = new InstitusjonellSektorkode(
                    sektorJson.get("kode").asText(),
                    sektorJson.get("beskrivelse").asText()
            );

            return new Underenhet(orgnr, overordnetEnhetOrgnr, navn, næringskode, sektor);

        } catch (IOException | NullPointerException e) {
            throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.");
        }
    }
}
