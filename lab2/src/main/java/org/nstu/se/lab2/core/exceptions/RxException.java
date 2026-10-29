package org.nstu.se.lab2.core.exceptions;

import java.io.Serial;

public class RxException extends Exception {
    @Serial private static final long serialVersionUID = 1L;

    public RxException(final String message) {
        super(message);
    }

    public RxException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
