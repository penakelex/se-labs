package org.nstu.se.lab2.core.reflection;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab2.annotations.RxParam;
import org.nstu.se.lab2.core.convert.ValueConverter;
import org.nstu.se.lab2.core.exceptions.ConverterException;
import org.nstu.se.lab2.core.exceptions.RxException;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;

public final class ArgumentBinder {
    private static final String ERROR_NO_CONVERTER = "Нет конвертера для ";
    private static final String ERROR_CONVERT = "Нельзя преобразовать '";
    private static final String QUOTED_IN = "' в ";
    private static final String ERROR_PARAMETER_MISSING = "Не задан параметр: ";
    private static final String ERROR_UNKNOWN_PARAMETER = "Лишний параметр: ";

    private ArgumentBinder() {}

    public static @Nullable Object[] bind(
            final Method method,
            final Map<String, String> values,
            final Map<Class<?>, ValueConverter> converters)
            throws RxException {
        final var parameters = method.getParameters();
        final var args = new @Nullable Object[parameters.length];
        final var used = new HashSet<String>();

        for (var index = 0; index < parameters.length; index++) {
            final var rxParam = parameters[index].getAnnotation(RxParam.class);

            if (rxParam == null) {
                args[index] = defaultValue(parameters[index].getType());
                continue;
            }

            final var raw = values.get(rxParam.name());
            if (raw == null) {
                throw new RxException(ERROR_PARAMETER_MISSING + rxParam.name());
            }

            used.add(rxParam.name());
            args[index] = convert(parameters[index].getType(), raw, converters);
        }

        for (final var key : values.keySet()) {
            if (!used.contains(key)) {
                throw new RxException(ERROR_UNKNOWN_PARAMETER + key);
            }
        }

        return args;
    }

    private static @Nullable Object defaultValue(final Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        return Array.get(Array.newInstance(type, 1), 0);
    }

    private static Object convert(
            final Class<?> type, final String raw, final Map<Class<?>, ValueConverter> converters)
            throws RxException {
        final var converter = converters.get(type);
        if (converter == null) {
            throw new RxException(ERROR_NO_CONVERTER + type.getSimpleName());
        }
        try {
            return converter.convert(raw);
        } catch (final ConverterException exception) {
            throw new RxException(
                    ERROR_CONVERT + raw + QUOTED_IN + type.getSimpleName(), exception);
        }
    }
}
