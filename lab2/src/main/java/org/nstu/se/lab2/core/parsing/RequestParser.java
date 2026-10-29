package org.nstu.se.lab2.core.parsing;

import org.nstu.se.lab2.core.RxRequest;
import org.nstu.se.lab2.core.exceptions.RxException;

public final class RequestParser {
    private static final String ERROR_EMPTY_REQUEST = "Пустой запрос";
    private static final String ERROR_NO_COLON = "Нет ':' в запросе: ";
    private static final String ERROR_EMPTY_METHOD = "Пустое имя метода в запросе: ";

    private RequestParser() {}

    public static RxRequest parse(final String request) throws RxException {
        if (request == null || request.isBlank()) {
            throw new RxException(ERROR_EMPTY_REQUEST);
        }

        final var trimmed = request.trim();
        final var colon = trimmed.indexOf(':');
        if (colon < 0) {
            throw new RxException(ERROR_NO_COLON + request);
        }

        final var methodName = trimmed.substring(0, colon).trim();
        if (methodName.isEmpty()) {
            throw new RxException(ERROR_EMPTY_METHOD + request);
        }

        final var rest = trimmed.substring(colon + 1).trim();
        return new RxRequest(methodName, ParameterParser.parse(rest));
    }
}
