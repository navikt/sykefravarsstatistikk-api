package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionUtils {
    public static <T> List<T> joinLists(Collection<T> list1, Collection<T> list2) {
        return Stream.concat(list1.stream(), list2.stream())
              .collect(Collectors.toList());
    }
}
