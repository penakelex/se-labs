package org.nstu.se.lab2.core.convert;

import org.nstu.se.lab2.core.exceptions.ConverterException;

@FunctionalInterface
public interface ValueConverter {
    Object convert(String raw) throws ConverterException;
}
