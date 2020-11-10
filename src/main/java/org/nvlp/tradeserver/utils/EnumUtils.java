package org.nvlp.tradeserver.utils;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnumUtils {

    public static <T, E extends Enum<E>> Map<T, E> createMapping(Class<E> clazz, Function<E, T> mapper) {
        EnumSet<E> es = EnumSet.allOf(clazz);
        Map<T, E> mapping = es.stream().collect(Collectors.toMap(mapper, Function.identity()));
        return mapping;
    }
}
