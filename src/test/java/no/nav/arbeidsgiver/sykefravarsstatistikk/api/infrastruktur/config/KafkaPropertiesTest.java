package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.SykefraværsstatistikkLocalTestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@EnableConfigurationProperties
//@ActiveProfiles("mvc-test")
@PropertySource("application-mvc-test.yaml")

class KafkaPropertiesTest {

    @Autowired
    KafkaProperties kafkaProperties;

    @BeforeEach
    public void setUp(){
//        kafkaProperties = new KafkaProperties();
    }

    @Test
    public void getTopicNavn__SkalReturnereRiktigTopicnavn(){
        //when(kafkaProperties.getTopic()).thenReturn()
        // TODO implemtn me //
        assertEquals(
              "arbeidsgiver.sykefravarsstatistikk-land-v1","arbeidsgiver.sykefravarsstatistikk-land-v1"
        );
    }
}
