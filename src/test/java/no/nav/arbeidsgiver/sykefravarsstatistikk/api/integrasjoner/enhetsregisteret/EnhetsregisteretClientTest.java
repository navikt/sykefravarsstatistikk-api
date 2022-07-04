package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.etOrgnr;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnhetsregisteretClientTest {

    private EnhetsregisteretClient enhetsregisteretClient;
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        enhetsregisteretClient = new EnhetsregisteretClient(restTemplate, "");
    }

    @Test
    public void hentInformasjonOmEnhet__skal_hente_riktige_felter() {
        mockRespons(gyldigEnhetRespons("999263550"));
        OverordnetEnhet overordnetEnhet = enhetsregisteretClient.hentInformasjonOmEnhet(new Orgnr("999263550"));

        assertThat(overordnetEnhet.getOrgnr().getVerdi()).isEqualTo("999263550");
        assertThat(overordnetEnhet.getNavn()).isEqualTo("NAV ARBEID OG YTELSER");
        assertThat(overordnetEnhet.getNæringskode().getKode()).isEqualTo("84300");
        assertThat(overordnetEnhet.getNæringskode().getBeskrivelse()).isEqualTo("Trygdeordninger underlagt offentlig forvaltning");
        assertThat(overordnetEnhet.getInstitusjonellSektorkode().getKode()).isEqualTo("6100");
        assertThat(overordnetEnhet.getInstitusjonellSektorkode().getBeskrivelse()).isEqualTo("Statsforvaltningen");
        assertThat(overordnetEnhet.getAntallAnsatte()).isEqualTo(40);
    }

    @Test
    public void hentInformasjonOmEnhet__skal_feile_hvis_server_returnerer_5xx_server_returnerer_5xx() {
        when(
                restTemplate.getForObject(
                        anyString(),
                        any()
                )
        ).thenThrow(
                new HttpServerErrorException(HttpStatus.BAD_GATEWAY)
        );

        assertThrows(EnhetsregisteretException.class, () -> enhetsregisteretClient.hentInformasjonOmEnhet(etOrgnr()));
    }

    @Test
    public void hentInformasjonOmEnhet__skal_feile_hvis_et_felt_mangler() {
        ObjectNode responsMedManglendeFelt = gyldigEnhetRespons("999263550");
        responsMedManglendeFelt.remove("institusjonellSektorkode");
        mockRespons(responsMedManglendeFelt);

        assertThrows(EnhetsregisteretMappingException.class, () -> enhetsregisteretClient.hentInformasjonOmEnhet(etOrgnr()));
    }

    @Test
    public void hentInformasjonOmEnhet__skal_feile_hvis_returnert_orgnr_ikke_matcher_med_medsendt_orgnr() {
        ObjectNode responsMedFeilOrgnr = gyldigEnhetRespons("999263550");
        mockRespons(responsMedFeilOrgnr);
        assertThrows(
                IllegalStateException.class,
                () -> enhetsregisteretClient.hentInformasjonOmEnhet(new Orgnr("777777777"))
        );
    }

    @Test
    public void hentInformasjonOmUnderenhet__skal_hente_riktige_felter() {
        mockRespons(gyldigUnderenhetRespons("971800534"));
        Underenhet underenhet = enhetsregisteretClient.hentUnderenhet(new Orgnr("971800534"));

        assertThat(underenhet.getOrgnr().getVerdi()).isEqualTo("971800534");
        assertThat(underenhet.getOverordnetEnhetOrgnr().getVerdi()).isEqualTo("999263550");
        assertThat(underenhet.getNavn()).isEqualTo("NAV ARBEID OG YTELSER AVD OSLO");
        assertThat(underenhet.getNæringskode().getKode()).isEqualTo("84300");
        assertThat(underenhet.getNæringskode().getBeskrivelse()).isEqualTo("Trygdeordninger underlagt offentlig forvaltning");
        assertThat(underenhet.getAntallAnsatte()).isEqualTo(40);
    }

    @Test
    public void hentInformasjonOmUnderenhet__skal_feile_hvis_server_returnerer_5xx() {
        when(
                restTemplate.getForObject(
                        anyString(),
                        any()
                )
        ).thenThrow(
                new HttpServerErrorException(HttpStatus.BAD_GATEWAY)
        );


        assertThrows(
                EnhetsregisteretIkkeTilgjengeligException.class,
                () -> enhetsregisteretClient.hentUnderenhet(etOrgnr())
        );
    }
    @Test
    public void hentInformasjonOmUnderenhet__skal_feile_hvis_et_felt_mangler() {
        ObjectNode responsMedManglendeFelt = gyldigUnderenhetRespons("822565212");
        responsMedManglendeFelt.remove("navn");
        mockRespons(responsMedManglendeFelt);

        assertThrows(
                EnhetsregisteretMappingException.class,
                () -> enhetsregisteretClient.hentUnderenhet(etOrgnr())
        );
    }

    @Test
    public void hentInformasjonOmUnderenhet__skal_feile_hvis_returnert_orgnr_ikke_matcher_med_medsendt_orgnr() {
        ObjectNode responsMedFeilOrgnr = gyldigUnderenhetRespons("822565212");
        mockRespons(responsMedFeilOrgnr);

        assertThrows(
                IllegalStateException.class,
                () -> enhetsregisteretClient.hentUnderenhet(new Orgnr("777777777"))
        );
    }

    @SneakyThrows
    private void mockRespons(JsonNode node) {
        when(restTemplate.getForObject(anyString(), any())).thenReturn(objectMapper.writeValueAsString(node));
    }

    @SneakyThrows
    private ObjectNode gyldigUnderenhetRespons(String orgnr) {
        String str = "{\n" +
                "  \"organisasjonsnummer\": \"" + orgnr + "\",\n" +
                "  \"navn\": \"NAV ARBEID OG YTELSER AVD OSLO\",\n" +
                "  \"naeringskode1\": {\n" +
                "    \"beskrivelse\": \"Trygdeordninger underlagt offentlig forvaltning\",\n" +
                "    \"kode\": \"84.300\"\n" +
                "  },\n" +
                "  \"antallAnsatte\": 40,\n" +
                "  \"overordnetEnhet\": \"999263550\"\n" +
                "}";
        return (ObjectNode) objectMapper.readTree(str);
    }

    @SneakyThrows
    private ObjectNode gyldigEnhetRespons(String orgnr) {
        String str = "{\n" +
                "  \"organisasjonsnummer\": \"" + orgnr + "\",\n" +
                "  \"navn\": \"NAV ARBEID OG YTELSER\",\n" +
                "  \"naeringskode1\": {\n" +
                "    \"beskrivelse\": \"Trygdeordninger underlagt offentlig forvaltning\",\n" +
                "    \"kode\": \"84.300\"\n" +
                "  },\n" +
                "  \"institusjonellSektorkode\": {\n" +
                "    \"kode\": \"6100\",\n" +
                "    \"beskrivelse\": \"Statsforvaltningen\"\n" +
                "  },\n" +
                "  \"antallAnsatte\": 40\n" +
                "}";
        return (ObjectNode) objectMapper.readTree(str);
    }
}

