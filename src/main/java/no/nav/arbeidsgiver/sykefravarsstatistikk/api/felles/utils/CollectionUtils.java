package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionUtils {

    public static <T> List<T> concat(Collection<T> collection1, Collection<T> collection2) {
        return Stream.concat(collection1.stream(), collection2.stream())
              .collect(Collectors.toList());
    }

    public static <T> List<T> concat(Collection<T> collection1, Collection<T> collection2, Collection<T> collection3) {
        return Stream.concat(Stream.concat(collection1.stream(), collection2.stream())
              , collection3.stream()).collect(Collectors.toList());
    }
}
