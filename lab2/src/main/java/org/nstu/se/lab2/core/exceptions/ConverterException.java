package org.nstu.se.lab2.core.exceptions;

import java.io.Serial;

public final class ConverterException extends Exception {
    @Serial private static final long serialVersionUID = 1L;

    public ConverterException(final Throwable cause) {
        super(cause);
    }
}
