package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class    EnhetsregisteretClient {
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

    public Enhet hentInformasjonOmEnhet(Orgnr orgnrTilEnhet) {
        String url = enhetsregisteretUrl + "enheter/" + orgnrTilEnhet.getVerdi();

        try {
            String respons = restTemplate.getForObject(url, String.class);
            return mapTilEnhet(respons);
        } catch (RestClientException e) {
            throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret", e);
        }
    }

    public Underenhet hentInformasjonOmUnderenhet(Orgnr orgnrTilUnderenhet) {
        String url = enhetsregisteretUrl + "underenheter/" + orgnrTilUnderenhet.getVerdi();

        try {
            String respons = restTemplate.getForObject(url, String.class);
            return mapTilUnderenhet(respons);
        } catch (RestClientException e) {
            throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret", e);
        }
    }

    private Enhet mapTilEnhet(String jsonResponseFraEnhetsregisteret) {
        try {
            JsonNode enhetJson = objectMapper.readTree(jsonResponseFraEnhetsregisteret);
            JsonNode næringskodeJson = enhetJson.get("naeringskode1");
            JsonNode sektorJson = enhetJson.get("institusjonellSektorkode");

            return new Enhet(
                    new Orgnr(enhetJson.get("organisasjonsnummer").textValue()),
                    enhetJson.get("navn").textValue(),
                    objectMapper.treeToValue(næringskodeJson, Næringskode5Siffer.class),
                    objectMapper.treeToValue(sektorJson, InstitusjonellSektorkode.class),
                    enhetJson.get("antallAnsatte").intValue()
            );

        } catch (IOException | NullPointerException e) {
            throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.", e);
        }
    }

    private Underenhet mapTilUnderenhet(String jsonResponseFraEnhetsregisteret) {
        try {
            JsonNode enhetJson = objectMapper.readTree(jsonResponseFraEnhetsregisteret);
            JsonNode næringskodeJson = enhetJson.get("naeringskode1");

            return new Underenhet(
                    new Orgnr(enhetJson.get("organisasjonsnummer").textValue()),
                    new Orgnr(enhetJson.get("overordnetEnhet").textValue()),
                    enhetJson.get("navn").textValue(),
                    objectMapper.treeToValue(næringskodeJson, Næringskode5Siffer.class),
                    enhetJson.get("antallAnsatte").intValue()
            );

        } catch (IOException | NullPointerException e) {
            throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.", e);
        }
    }

    public HttpStatus healthcheck() {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    enhetsregisteretUrl,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    String.class
            );
            return response.getStatusCode();
        } catch (RestClientResponseException e) {
            return HttpStatus.valueOf(e.getRawStatusCode());
        }
    }
}
