package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils;

import static java.util.stream.Collectors.toList;

import io.vavr.control.Either;
import java.util.List;
import java.util.stream.Stream;

public class EitherUtils {

    @SafeVarargs
    public static <L, R> List<R> filterRights(Either<L, R>... leftsAndRights) {
        return Stream.of(leftsAndRights)
                .filter(Either::isRight)
                .map(Either::get)
                .collect(toList());
    }
}
