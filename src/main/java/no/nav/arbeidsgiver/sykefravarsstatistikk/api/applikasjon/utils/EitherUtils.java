package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils;

import static java.util.stream.Collectors.toList;

import io.vavr.control.Either;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EitherUtils {

  @SafeVarargs
  public static <L, R> List<R> getRightsAndLogLefts(Either<L, R>... leftsAndRights) {
    Stream.of(leftsAndRights)
        .filter(Either::isLeft)
        .forEach(feil -> log.warn("Følgende avvik ble registrert: " + feil.getLeft()));

    return Stream.of(leftsAndRights).filter(Either::isRight).map(Either::get).collect(toList());
  }
}