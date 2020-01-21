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

import static no.nav.tag.sykefravarsstatistikk.api.TestData.etOrgnr;
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
    public void hentInformasjonOmEnhet__skal_hente_riktige_felter() {
        mockRespons(gyldigEnhetRespons());
        Enhet enhet = enhetsregisteretClient.hentInformasjonOmEnhet(etOrgnr());

        assertThat(enhet.getOrgnr().getVerdi()).isEqualTo("999263550");
        assertThat(enhet.getNavn()).isEqualTo("NAV ARBEID OG YTELSER");
        assertThat(enhet.getNæringskode().getKode()).isEqualTo("84300");
        assertThat(enhet.getNæringskode().getBeskrivelse()).isEqualTo("Trygdeordninger underlagt offentlig forvaltning");
        assertThat(enhet.getInstitusjonellSektorkode().getKode()).isEqualTo("6100");
        assertThat(enhet.getInstitusjonellSektorkode().getBeskrivelse()).isEqualTo("Statsforvaltningen");
        assertThat(enhet.getAntallAnsatte()).isEqualTo(40);
    }

    @Test(expected = EnhetsregisteretException.class)
    public void hentInformasjonOmEnhet__skal_feile_hvis_et_felt_mangler() {
        ObjectNode responsMedManglendeFelt = gyldigEnhetRespons();
        responsMedManglendeFelt.remove("institusjonellSektorkode");
        mockRespons(responsMedManglendeFelt);

        enhetsregisteretClient.hentInformasjonOmUnderenhet(etOrgnr());
    }

    @Test
    public void hentInformasjonOmUnderenhet__skal_hente_riktige_felter() {
        mockRespons(gyldigUnderenhetRespons());
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(new Orgnr("971800534"));

        assertThat(underenhet.getOrgnr().getVerdi()).isEqualTo("822565212");
        assertThat(underenhet.getOverordnetEnhetOrgnr().getVerdi()).isEqualTo("999263550");
        assertThat(underenhet.getNavn()).isEqualTo("NAV ARBEID OG YTELSER AVD OSLO");
        assertThat(underenhet.getNæringskode().getKode()).isEqualTo("84300");
        assertThat(underenhet.getNæringskode().getBeskrivelse()).isEqualTo("Trygdeordninger underlagt offentlig forvaltning");
        assertThat(underenhet.getAntallAnsatte()).isEqualTo(40);
    }

    @Test(expected = EnhetsregisteretException.class)
    public void hentInformasjonOmUnderenhet__skal_feile_hvis_et_felt_mangler() {
        ObjectNode responsMedManglendeFelt = gyldigUnderenhetRespons();
        responsMedManglendeFelt.remove("navn");
        mockRespons(responsMedManglendeFelt);

        enhetsregisteretClient.hentInformasjonOmUnderenhet(etOrgnr());
    }

    @SneakyThrows
    private void mockRespons(JsonNode node) {
        when(restTemplate.getForObject(anyString(), any())).thenReturn(objectMapper.writeValueAsString(node));
    }

    @SneakyThrows
    private ObjectNode gyldigUnderenhetRespons() {
        String str = "{\n" +
                "  \"organisasjonsnummer\": \"822565212\",\n" +
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
    private ObjectNode gyldigEnhetRespons() {
        String str = "{\n" +
                "  \"organisasjonsnummer\": \"999263550\",\n" +
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
