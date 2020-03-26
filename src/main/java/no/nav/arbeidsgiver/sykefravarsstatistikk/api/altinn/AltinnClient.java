package no.nav.arbeidsgiver.sykefravarsstatistikk.api.altinn;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Fnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.feratureToggles.FeatureToggleService;
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

import static java.lang.String.format;

@Slf4j
@Component
public class AltinnClient {
    private final RestTemplate restTemplate;

    private final String altinnUrl;
    private final String altinnProxyUrl;
    private final String altinnAPIGWApikey;
    private final String altinnHeader;

    private final String iawebServiceCode;
    private final String iawebServiceEdition;

    private final TilgangskontrollUtils tilgangskontrollUtils;
    private final FeatureToggleService featureToggles;

    public AltinnClient(
            RestTemplate restTemplate,
            @Value("${altinn.url}") String altinnUrl,
            @Value("${altinn.proxy.url}") String altinnProxyUrl,
            @Value("${altinn.apigw.apikey}") String altinnAPIGWApikey,
            @Value("${altinn.apikey}") String altinnApikey,
            @Value("${altinn.iaweb.service.code}") String iawebServiceCode,
            @Value("${altinn.iaweb.service.edition}") String iawebServiceEdition,
            TilgangskontrollUtils tilgangskontrollUtils,
            FeatureToggleService featureToggles
    ) {
        this.restTemplate = restTemplate;
        this.altinnUrl = altinnUrl;
        this.altinnProxyUrl = altinnProxyUrl;
        this.altinnAPIGWApikey = altinnAPIGWApikey;
        this.altinnHeader = altinnApikey;
        this.iawebServiceCode = iawebServiceCode;
        this.iawebServiceEdition = iawebServiceEdition;
        this.tilgangskontrollUtils = tilgangskontrollUtils;
        this.featureToggles = featureToggles;
    }

    public List<AltinnOrganisasjon> hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(Fnr fnr) {
        HttpHeaders headers;
        URI uri;

        if (featureToggles.erEnabled("arbeidsgiver.sykefravarsstatikk-api.bruk-altinn-proxy")) {
            headers = getAuthHeadersForInnloggetBruker();
            uri = getProxyURI();
        } else {
            headers = getAuthHeadersMotAltinn();
            uri = getAltinnURI(fnr);
        }

        try {
            Optional<List<AltinnOrganisasjon>> respons = Optional.ofNullable(restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
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

    private URI getProxyURI() {
        return UriComponentsBuilder.fromUriString(altinnProxyUrl)
                .pathSegment("ekstern", "altinn", "api", "serviceowner", "reportees")
                .queryParam("ForceEIAuthentication")
                .queryParam("serviceCode", iawebServiceCode)
                .queryParam("serviceEdition", iawebServiceEdition)
                .build()
                .toUri();
    }

    private URI getAltinnURI(Fnr fnr) {
        return UriComponentsBuilder.fromUriString(altinnUrl)
                .pathSegment("ekstern", "altinn", "api", "serviceowner", "reportees")
                .queryParam("ForceEIAuthentication")
                .queryParam("subject", fnr.getVerdi())
                .queryParam("serviceCode", iawebServiceCode)
                .queryParam("serviceEdition", iawebServiceEdition)
                .build()
                .toUri();
    }

    public List<AltinnRolle> hentRoller(Fnr fnr, Orgnr orgnr) {
        URI uri = UriComponentsBuilder.fromUriString(altinnUrl)
                .pathSegment("ekstern", "altinn", "api", "serviceowner", "authorization", "roles")
                .queryParam("ForceEIAuthentication")
                .queryParam("subject", fnr.getVerdi())
                .queryParam("reportee", orgnr.getVerdi())
                .build()
                .toUri();
        try {
            Optional<List<AltinnRolle>> respons = Optional.ofNullable(restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    getHeaderEntity(),
                    new ParameterizedTypeReference<List<AltinnRolle>>() {
                    }
            ).getBody());

            if (respons.isPresent()) {
                List<AltinnRolle> altinnRoller = respons.get();
                log.info(format("Hentet %d roller fra Altinn", altinnRoller.size()));
                return altinnRoller;
            } else {
                throw new AltinnException("Feil ved kall til Altinn. Response body er null.");
            }

        } catch (RestClientException e) {
            throw new AltinnException("Feil ved kall til Altinn", e);
        }
    }


    private HttpEntity<HttpHeaders> getHeaderEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NAV-APIKEY", altinnAPIGWApikey);
        headers.set("APIKEY", altinnHeader);
        return new HttpEntity<>(headers);
    }

    private HttpHeaders getAuthHeadersForInnloggetBruker() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tilgangskontrollUtils.getSelvbetjeningToken());
        return headers;
    }

    private HttpHeaders getAuthHeadersMotAltinn() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NAV-APIKEY", altinnAPIGWApikey);
        headers.set("APIKEY", altinnHeader);
        return headers;
    }
}
