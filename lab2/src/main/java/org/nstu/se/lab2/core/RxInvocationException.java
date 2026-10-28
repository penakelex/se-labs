package org.nstu.se.lab2.core;

import java.io.Serial;

public class RxInvocationException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1L;

    public RxInvocationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
