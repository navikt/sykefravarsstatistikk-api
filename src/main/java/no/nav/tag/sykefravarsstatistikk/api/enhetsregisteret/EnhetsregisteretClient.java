package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Slf4j
@Component
public class EnhetsregisteretClient {
    private final RestTemplate restTemplate;
    private final String enhetsregisteretUrl;

    public EnhetsregisteretClient(
            RestTemplate restTemplate,
            @Value("${enhetsregisteret.url}") String enhetsregisteretUrl
  ) {
        this.restTemplate = restTemplate;
        this.enhetsregisteretUrl = enhetsregisteretUrl;
    }

    public String hentEnhetsinformasjon(Orgnr orgnr) {
        URI uri = UriComponentsBuilder.fromUriString(enhetsregisteretUrl)
                .pathSegment("enheter")
                .queryParam("organisasjonsnummer", orgnr.getVerdi())
                .build()
                .toUri();

        try {
            Optional<Object> respons = Optional.ofNullable(restTemplate.getForObject(uri, Object.class));

            if (respons.isPresent()) {
                return respons.get().toString();
            } else {
                throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret. Response body er null.");
            }

        } catch (RestClientException e) {
            log.error("Feil ved kall til Enhetsregisteret", e);
            throw new EnhetsregisteretException("Feil ved kall til Enhetsregisteret", e);
        }
    }
}
