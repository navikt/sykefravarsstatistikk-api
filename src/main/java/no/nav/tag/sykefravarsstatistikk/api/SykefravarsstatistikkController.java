package no.nav.tag.sykefravarsstatistikk.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class SykefravarsstatistikkController {

    @Autowired
    public SykefravarsstatistikkController(){
    }

    @GetMapping(value = "/status")
    public ResponseEntity status() {
            return ResponseEntity.status(HttpStatus.OK).body("RUNNING");
    }

}
