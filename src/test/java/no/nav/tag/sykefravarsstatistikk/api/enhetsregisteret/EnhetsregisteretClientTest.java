package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.getOrgnr;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnhetsregisteretClientTest {

    private EnhetsregisteretClient enhetsregisteretClient;
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setup() {
        enhetsregisteretClient = new EnhetsregisteretClient(restTemplate, "");
    }

    @Test
    public void hentEnhetsinformasjon__skal_hente_riktige_felter() {
        mockRespons(gyldigUnderenhetRespons());
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(new Orgnr("971800534"));

        assertThat(underenhet.getOrgnr().getVerdi()).isEqualTo("894834412");
        assertThat(underenhet.getOverordnetEnhetOrgnr().getVerdi()).isEqualTo("999263550");
        assertThat(underenhet.getNavn()).isEqualTo("NAV ARBEID OG YTELSER AVD OSLO");
        assertThat(underenhet.getNæringskode().getKode()).isEqualTo("84300");
        assertThat(underenhet.getNæringskode().getBeskrivelse()).isEqualTo("Trygdeordninger underlagt offentlig forvaltning");
    }

    @Test(expected = EnhetsregisteretException.class)
    public void hentEnhetsinformasjon__skal_feile_hvis_navn_mangler() {
        mockRespons(gyldigUnderenhetResponsUtenFelt("navn"));
        enhetsregisteretClient.hentInformasjonOmUnderenhet(getOrgnr());
    }

    @Test(expected = EnhetsregisteretException.class)
    public void hentEnhetsinformasjon__skal_feile_hvis_orgnr_mangler() {
        mockRespons(gyldigUnderenhetResponsUtenFelt("organisasjonsnummer"));
        enhetsregisteretClient.hentInformasjonOmUnderenhet(getOrgnr());
    }

    @Test(expected = EnhetsregisteretException.class)
    public void hentEnhetsinformasjon__skal_feile_hvis_overordnet_næringskode_mangler() {
        mockRespons(gyldigUnderenhetResponsUtenFelt("naeringskode1"));
        enhetsregisteretClient.hentInformasjonOmUnderenhet(getOrgnr());
    }

    @SneakyThrows
    private void mockRespons(JsonNode node) {
        when(restTemplate.getForObject(anyString(), any())).thenReturn(objectMapper.writeValueAsString(node));
    }

    private ObjectNode gyldigUnderenhetResponsUtenFelt(String felt) {
        ObjectNode node = gyldigUnderenhetRespons();
        node.remove(felt);
        return node;
    }

    @SneakyThrows
    private ObjectNode gyldigUnderenhetRespons() {
        String str = "{\n" +
                "  \"organisasjonsnummer\": \"894834412\",\n" +
                "  \"navn\": \"NAV ARBEID OG YTELSER AVD OSLO\",\n" +
                "  \"naeringskode1\": {\n" +
                "    \"beskrivelse\": \"Trygdeordninger underlagt offentlig forvaltning\",\n" +
                "    \"kode\": \"84.300\"\n" +
                "  },\n" +
                "  \"overordnetEnhet\": \"999263550\"\n" +
                "}";
        return (ObjectNode) objectMapper.readTree(str);
    }
}