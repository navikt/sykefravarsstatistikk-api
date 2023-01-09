package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.SelvbetjeningToken;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.ServiceCode;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.ServiceEdition;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Fnr;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AltinnKlientWrapper {

  private final AltinnrettigheterProxyKlient klient;
  private final String serviceCode;
  private final String serviceEdition;

  public AltinnKlientWrapper(
      @Value("${altinn.proxy.url}") String altinnProxyUrl,
      @Value("${altinn.url}") String altinnUrl,
      @Value("${altinn.apigw.apikey}") String altinnAPIGWApikey,
      @Value("${altinn.apikey}") String altinnApikey,
      @Value("${altinn.iaweb.service.code}") String iawebServiceCode,
      @Value("${altinn.iaweb.service.edition}") String iawebServiceEdition) {

    this.serviceCode = iawebServiceCode;
    this.serviceEdition = iawebServiceEdition;

    AltinnrettigheterProxyKlientConfig config =
        new AltinnrettigheterProxyKlientConfig(
            new ProxyConfig("sykefraværsstatistikk", altinnProxyUrl),
            new AltinnConfig(altinnUrl, altinnApikey, altinnAPIGWApikey));

    this.klient = new AltinnrettigheterProxyKlient(config);
  }

  public List<AltinnOrganisasjon> hentOrgnumreDerBrukerHarEnkeltrettighetTilIAWeb(
      JwtToken idToken, Fnr fnr) {
    return mapTo(
        klient.hentOrganisasjoner(
            new SelvbetjeningToken(idToken.getTokenAsString()),
            new Subject(fnr.getVerdi()),
            new ServiceCode(serviceCode),
            new ServiceEdition(serviceEdition),
            true));
  }

  public List<AltinnOrganisasjon> hentOrgnumreDerBrukerHarTilgangTil(JwtToken idToken, Fnr fnr) {
    return mapTo(
        klient.hentOrganisasjoner(
            new SelvbetjeningToken(idToken.getTokenAsString()), new Subject(fnr.getVerdi()), true));
  }

  private List<AltinnOrganisasjon> mapTo(List<AltinnReportee> altinnReportees) {
    return altinnReportees.stream()
        .map(
            org -> {
              AltinnOrganisasjon altinnOrganisasjon = new AltinnOrganisasjon();
              altinnOrganisasjon.setName(org.getName());
              altinnOrganisasjon.setType(org.getType());
              altinnOrganisasjon.setParentOrganizationNumber(org.getParentOrganizationNumber());
              altinnOrganisasjon.setOrganizationNumber(org.getOrganizationNumber());
              altinnOrganisasjon.setOrganizationForm(org.getOrganizationForm());
              altinnOrganisasjon.setStatus(org.getStatus());

              return altinnOrganisasjon;
            })
        .collect(Collectors.toList());
  }
}
