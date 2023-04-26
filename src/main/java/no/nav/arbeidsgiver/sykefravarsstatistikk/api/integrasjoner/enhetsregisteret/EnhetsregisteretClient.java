package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.InstitusjonellSektorkode;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
public class EnhetsregisteretClient {

  private final RestTemplate restTemplate;
  private final String enhetsregisteretUrl;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public EnhetsregisteretClient(
      RestTemplate restTemplate, @Value("${enhetsregisteret.url}") String enhetsregisteretUrl) {
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
          enhetJson.get("antallAnsatte").intValue());

    } catch (IOException | NullPointerException | IllegalArgumentException e) {
      throw new EnhetsregisteretMappingException(
          "Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.", e);
    }
  }

  private void validerReturnertOrgnr(Orgnr opprinneligOrgnr, Orgnr returnertOrgnr) {
    if (!opprinneligOrgnr.equals(returnertOrgnr)) {
      throw new OrgnrEksistererIkkeException(
          "Orgnr hentet fra Enhetsregisteret samsvarer ikke med det medsendte orgnr. Request: "
              + opprinneligOrgnr.getVerdi()
              + ", response: "
              + returnertOrgnr.getVerdi());
    }
  }

  private Underenhet mapTilUnderenhet(String jsonResponseFraEnhetsregisteret) {
    try {
      JsonNode enhetJson = objectMapper.readTree(jsonResponseFraEnhetsregisteret);

      Næringskode5Siffer primærnæringskode =
          Bransjeprogram.velgPrimærnæringskode(
              List.of("naeringskode1", "naeringskode2", "naeringskode3").stream()
                  .map(enhetJson::get)
                  .filter(Objects::nonNull)
                  .map(json -> objectMapper.convertValue(json, Næringskode5Siffer.class))
                  .collect(Collectors.toList()));

      return new Underenhet(
          new Orgnr(enhetJson.get("organisasjonsnummer").textValue()),
          new Orgnr(enhetJson.get("overordnetEnhet").textValue()),
          enhetJson.get("navn").textValue(),
          primærnæringskode,
          enhetJson.get("antallAnsatte").intValue());

    } catch (IOException | NullPointerException e) {
      throw new EnhetsregisteretMappingException(
          "Feil ved kall til Enhetsregisteret. Kunne ikke parse respons.", e);
    }
  }

  }

  public HttpStatusCode healthcheck() {
    try {
      ResponseEntity<String> response =
          restTemplate.exchange(
              enhetsregisteretUrl, HttpMethod.GET, HttpEntity.EMPTY, String.class);
      return response.getStatusCode();
    } catch (RestClientResponseException e) {
      return HttpStatus.valueOf(e.getStatusCode().value());
    }
  }
}
