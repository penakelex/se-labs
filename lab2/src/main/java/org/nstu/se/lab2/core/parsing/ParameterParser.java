package org.nstu.se.lab2.core.parsing;

import org.nstu.se.lab2.core.exceptions.RxException;

import java.util.HashMap;
import java.util.Map;

final class ParameterParser {
    private static final char CLOSING_QUOTE = '\'';
    private static final char DOUBLE_QUOTE = '"';
    private static final char BACKSLASH = '\\';
    private static final char EQUALS = '=';
    private static final char COMMA = ',';
    private static final char SEMICOLON = ';';

    private static final String ERROR_NO_EQUALS = "Нет '=' в паре: '";
    private static final String ERROR_EMPTY_PARAMETER = "Пустое имя параметра в паре: '";
    private static final String ERROR_DUPLICATE_PARAMETER = "Дублирующийся параметр: ";
    private static final String ERROR_UNTERMINATED_QUOTE = "Незакрытая кавычка в паре: '";
    private static final String ERROR_TRAILING_AFTER_QUOTE =
            "Лишние символы после закрывающей кавычки: '";

    private ParameterParser() {}

    static Map<String, String> parse(final String rest) throws RxException {
        final var parameters = new HashMap<String, String>();

        if (rest.isEmpty()) {
            return parameters;
        }

        var index = 0;
        final var length = rest.length();

        while (index < length) {
            index = skipSeparatorsAndWhitespace(rest, index);
            if (index >= length) {
                break;
            }

            final var end = findParameterEnd(rest, index);
            final var part = rest.substring(index, end).trim();

            if (!part.isEmpty()) {
                final var keyValue = splitPair(part);
                putParameter(parameters, keyValue[0], keyValue[1]);
            }

            index = end + 1;
        }

        return parameters;
    }

    private static int skipSeparatorsAndWhitespace(final String text, final int from) {
        var index = from;
        while (index < text.length() && isSeparatorOrWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }

    private static int findParameterEnd(final String text, final int from) throws RxException {
        var inQuotes = false;
        var escaped = false;

        for (var index = from; index < text.length(); index++) {
            final var character = text.charAt(index);

            if (escaped) {
                escaped = false;
            } else if (isEscape(character, inQuotes)) {
                escaped = true;
            } else if (isQuote(character)) {
                inQuotes = !inQuotes;
            } else if (!inQuotes && isSeparator(character)) {
                return index;
            }
        }

        if (inQuotes) {
            throw new RxException(ERROR_UNTERMINATED_QUOTE + text.substring(from) + "'");
        }

        return text.length();
    }

    private static boolean isEscape(final char character, final boolean inQuotes) {
        return character == BACKSLASH && inQuotes;
    }

    private static boolean isQuote(final char character) {
        return character == DOUBLE_QUOTE;
    }

    private static boolean isSeparatorOrWhitespace(final char character) {
        return isSeparator(character) || Character.isWhitespace(character);
    }

    private static boolean isSeparator(final char character) {
        return character == COMMA || character == SEMICOLON;
    }

    private static String[] splitPair(final String pair) throws RxException {
        final var equalsIndex = findEquals(pair);
        if (equalsIndex < 0) {
            throw new RxException(ERROR_NO_EQUALS + pair + CLOSING_QUOTE);
        }

        final var key = pair.substring(0, equalsIndex).trim();
        if (key.isEmpty()) {
            throw new RxException(ERROR_EMPTY_PARAMETER + pair + CLOSING_QUOTE);
        }

        final var rawValue = pair.substring(equalsIndex + 1).trim();
        return new String[] {key, parseValue(rawValue, pair)};
    }

    private static int findEquals(final String pair) {
        var inQuotes = false;
        var escaped = false;

        for (var index = 0; index < pair.length(); index++) {
            final var character = pair.charAt(index);

            if (escaped) {
                escaped = false;
            } else if (isEscape(character, inQuotes)) {
                escaped = true;
            } else if (isQuote(character)) {
                inQuotes = !inQuotes;
            } else if (character == EQUALS && !inQuotes) {
                return index;
            }
        }

        return -1;
    }

    private static String parseValue(final String rawValue, final String pair) throws RxException {
        if (!isQuoted(rawValue)) {
            return rawValue;
        }

        final var value = new StringBuilder();
        var escaped = false;

        for (var index = 1; index < rawValue.length(); index++) {
            final var character = rawValue.charAt(index);

            if (escaped) {
                value.append(unescape(character));
                escaped = false;
            } else if (character == BACKSLASH) {
                escaped = true;
            } else if (isQuote(character)) {
                return finishQuotedValue(rawValue, index, pair, value);
            } else {
                value.append(character);
            }
        }

        throw new RxException(ERROR_UNTERMINATED_QUOTE + pair + CLOSING_QUOTE);
    }

    private static boolean isQuoted(final String rawValue) {
        return !rawValue.isEmpty() && rawValue.charAt(0) == DOUBLE_QUOTE;
    }

    private static String finishQuotedValue(
            final String rawValue,
            final int quoteIndex,
            final String pair,
            final StringBuilder value)
            throws RxException {
        final var trailing = rawValue.substring(quoteIndex + 1);
        if (!trailing.isBlank()) {
            throw new RxException(ERROR_TRAILING_AFTER_QUOTE + pair + CLOSING_QUOTE);
        }
        return value.toString();
    }

    private static char unescape(final char character) {
        return switch (character) {
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case '"' -> DOUBLE_QUOTE;
            case '\\' -> BACKSLASH;
            default -> character;
        };
    }

    private static void putParameter(
            final Map<String, String> parameters, final String key, final String value)
            throws RxException {
        if (parameters.containsKey(key)) {
            throw new RxException(ERROR_DUPLICATE_PARAMETER + key);
        }
        parameters.put(key, value);
    }
}
