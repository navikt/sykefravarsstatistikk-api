package no.nav.tag.sykefravarsstatistikk.api.altinn;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AltinnClient {
    private final RestTemplate restTemplate;

    private final String altinnUrl;
    private final String altinnAPIGWApikey;
    private final String altinnHeader;

    private final static String ENKELTTJENESTE_TJENESTEKODE_IAWEB = "3403";
    private final static String ENKELTTJENESTE_TJENESTEKODE_VERSJON = "2";

    public AltinnClient(
            RestTemplate restTemplate,
            @Value("${altinn.url}") String altinnUrl,
            @Value("${altinn.apigw.apikey}") String altinnAPIGWApikey,
            @Value("${altinn.apikey}") String altinnApikey
    ) {
        this.restTemplate = restTemplate;
        this.altinnUrl = altinnUrl;
        this.altinnAPIGWApikey = altinnAPIGWApikey;
        this.altinnHeader = altinnApikey;
    }

    public List<String> hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(String fnr) {
        URI uri = UriComponentsBuilder.fromUriString(altinnUrl)
                .pathSegment("ekstern", "altinn", "api", "serviceowner", "reportees")
                .queryParam("ForceEIAuthentication")
                .queryParam("subject", fnr)
                .queryParam("serviceCode", ENKELTTJENESTE_TJENESTEKODE_IAWEB)
                .queryParam("serviceEdition", ENKELTTJENESTE_TJENESTEKODE_VERSJON)
                .build()
                .toUri();

        try {
            Optional<List<Organisasjon>> respons = Optional.ofNullable(restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    getHeaderEntity(),
                    new ParameterizedTypeReference<List<Organisasjon>>() {}
            ).getBody());

            if (respons.isPresent()) {
                return respons.get()
                        .stream()
                        .map(Organisasjon::getOrganizationNumber)
                        .collect(Collectors.toList());
            } else {
                throw new AltinnException("Feil ved kall til Altinn. Response body er null.");
            }

        } catch (RestClientException e) {
            log.error("Feil ved kall til Altinn", e);
            throw new AltinnException("Feil ved kall til Altinn", e);
        }
    }

    private HttpEntity<Object> getHeaderEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NAV-APIKEY", altinnAPIGWApikey);
        headers.set("APIKEY", altinnHeader);
        return new HttpEntity<>(headers);
    }
}
