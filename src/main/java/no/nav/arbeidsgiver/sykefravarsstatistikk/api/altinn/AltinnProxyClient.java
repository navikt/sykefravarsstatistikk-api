package no.nav.arbeidsgiver.sykefravarsstatistikk.api.altinn;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Fnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollUtils;
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

@Slf4j
@Component
public class AltinnProxyClient {
    private final RestTemplate restTemplate;
    private final TilgangskontrollUtils tilgangskontrollUtils;
    private final String altinnUrl;
    private final String iawebServiceCode;
    private final String iawebServiceEdition;

    public AltinnProxyClient(
            RestTemplate restTemplate,
            TilgangskontrollUtils tilgangskontrollUtils,
            @Value("${altinn.url}") String altinnUrl,
            @Value("${altinn.iaweb.service.code}") String iawebServiceCode,
            @Value("${altinn.iaweb.service.edition}") String iawebServiceEdition
    ) {
        this.restTemplate = restTemplate;
        this.tilgangskontrollUtils = tilgangskontrollUtils;
        this.altinnUrl = altinnUrl;
        this.iawebServiceCode = iawebServiceCode;
        this.iawebServiceEdition = iawebServiceEdition;
    }

    public List<AltinnOrganisasjon> hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(Fnr fnr) {
        URI uri = UriComponentsBuilder.fromUriString(altinnUrl)
                .pathSegment("ekstern", "altinn", "api", "serviceowner", "reportees")
                .queryParam("ForceEIAuthentication")
                .queryParam("subject", fnr.getVerdi())
                .queryParam("serviceCode", iawebServiceCode)
                .queryParam("serviceEdition", iawebServiceEdition)
                .build()
                .toUri();
        try {
            Optional<List<AltinnOrganisasjon>> respons = Optional.ofNullable(restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    getHeaderEntity(),
                    new ParameterizedTypeReference<List<AltinnOrganisasjon>>() {
                    }
            ).getBody());

            if (respons.isPresent()) {
                return respons.get();
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
        headers.setBearerAuth(tilgangskontrollUtils.getSelvbetjeningToken());
        return new HttpEntity<>(headers);
    }
}
