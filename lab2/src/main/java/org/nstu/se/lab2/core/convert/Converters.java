package org.nstu.se.lab2.core.convert;

import org.nstu.se.lab2.core.RxService;
import org.nstu.se.lab2.core.exceptions.ConverterException;

public final class Converters {
    private Converters() {}

    public static void registerInt(final RxService service) {
        service.registerConverter(int.class, Converters::toInt);
        service.registerConverter(Integer.class, Converters::toInt);
    }

    public static void registerDouble(final RxService service) {
        service.registerConverter(double.class, Converters::toDouble);
        service.registerConverter(Double.class, Converters::toDouble);
    }

    public static void registerString(final RxService service) {
        service.registerConverter(String.class, value -> value);
    }

    private static int toInt(final String value) throws ConverterException {
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException exception) {
            throw new ConverterException(exception);
        }
    }

    private static double toDouble(final String value) throws ConverterException {
        try {
            return Double.parseDouble(value);
        } catch (final NumberFormatException exception) {
            throw new ConverterException(exception);
        }
    }
}
