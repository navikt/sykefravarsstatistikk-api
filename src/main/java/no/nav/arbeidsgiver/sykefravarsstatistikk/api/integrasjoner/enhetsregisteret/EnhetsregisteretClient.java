package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.InstitusjonellSektorkode;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
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

    public OverordnetEnhet hentInformasjonOmEnhet(Orgnr orgnrTilEnhet) {
        String url = enhetsregisteretUrl + "enheter/" + orgnrTilEnhet.getVerdi();

        try {
            String respons = restTemplate.getForObject(url, String.class);
            OverordnetEnhet overordnetEnhet = mapTilEnhet(respons);
            validerReturnertOrgnr(orgnrTilEnhet, overordnetEnhet.getOrgnr());
            return overordnetEnhet;
        } catch (RestClientException e) {
            throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret", e);
        }
    }

    public Underenhet hentInformasjonOmUnderenhet(Orgnr orgnrTilUnderenhet) {
        try {
            String url = enhetsregisteretUrl + "underenheter/" + orgnrTilUnderenhet.getVerdi();
            String respons = restTemplate.getForObject(url, String.class);
            Underenhet underenhet = mapTilUnderenhet(respons);
            validerReturnertOrgnr(orgnrTilUnderenhet, underenhet.getOrgnr());
            return underenhet;
        } catch (HttpServerErrorException hsee) {
            throw new EnhetsregisteretIkkeTilgjengeligException("Enhetsregisteret svarer ikke", hsee);
        } catch (RestClientException e) {
            throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret", e);
        }
    }

    private OverordnetEnhet mapTilEnhet(String jsonResponseFraEnhetsregisteret) {
        try {
            JsonNode enhetJson = objectMapper.readTree(jsonResponseFraEnhetsregisteret);
            JsonNode næringskodeJson = enhetJson.get("naeringskode1");
            JsonNode sektorJson = enhetJson.get("institusjonellSektorkode");

            return new OverordnetEnhet(
                    new Orgnr(enhetJson.get("organisasjonsnummer").textValue()),
                    enhetJson.get("navn").textValue(),
                    objectMapper.treeToValue(næringskodeJson, Næringskode5Siffer.class),
                    objectMapper.treeToValue(sektorJson, InstitusjonellSektorkode.class),
                    enhetJson.get("antallAnsatte").intValue()
            );

        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            throw new EnhetsregisteretMappingException("Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.", e);
        }
    }

    private void validerReturnertOrgnr(Orgnr opprinneligOrgnr, Orgnr returnertOrgnr) {
        if (!opprinneligOrgnr.equals(returnertOrgnr)) {
            throw new IllegalStateException(
                    "Orgnr hentet fra Enhetsregisteret samsvarer ikke med det medsendte orgnr. Request: "
                            + opprinneligOrgnr.getVerdi()
                            + ", response: "
                            + returnertOrgnr.getVerdi()
            );
        }
    }

    private Underenhet mapTilUnderenhet(String jsonResponseFraEnhetsregisteret) {
        try {
            JsonNode enhetJson = objectMapper.readTree(jsonResponseFraEnhetsregisteret);
            JsonNode næringskodeJson = enhetJson.get("naeringskode1");

            if (næringskodeJson == null) {
                throw new IngenNæringException("Feil ved kall til Enhetsregisteret. Ingen næring for virksomhet.");
            }

            return new Underenhet(
                    new Orgnr(enhetJson.get("organisasjonsnummer").textValue()),
                    new Orgnr(enhetJson.get("overordnetEnhet").textValue()),
                    enhetJson.get("navn").textValue(),
                    objectMapper.treeToValue(næringskodeJson, Næringskode5Siffer.class),
                    enhetJson.get("antallAnsatte").intValue()
            );

        } catch (IOException | NullPointerException e) {
            throw new EnhetsregisteretMappingException("Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.", e);
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
