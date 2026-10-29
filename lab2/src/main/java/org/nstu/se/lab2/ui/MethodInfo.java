package org.nstu.se.lab2.ui;

import org.nstu.se.lab2.Messages;
import org.nstu.se.lab2.core.MethodDescriptor;

public record MethodInfo(String name, String info, String parameters, String exampleRequest) {
    private static final String KEY_VALUE_SEPARATOR = ":";
    private static final String PARAM_ASSIGNMENT = "=";
    private static final String PAIR_SEPARATOR = ",";
    private static final String PARAMETER_SEPARATOR = ", ";
    private static final String NO_PARAMETERS = "—";

    static MethodInfo from(final MethodDescriptor source) {
        final var params = source.parameters();

        final var paramText =
                params.stream()
                        .map(
                                parameter ->
                                        parameter.name()
                                                + KEY_VALUE_SEPARATOR
                                                + parameter.type().getSimpleName())
                        .reduce((left, right) -> left + PARAMETER_SEPARATOR + right)
                        .orElse(NO_PARAMETERS);

        final var example =
                params.stream()
                        .map(
                                parameter ->
                                        parameter.name()
                                                + PARAM_ASSIGNMENT
                                                + exampleValue(parameter.type().getSimpleName()))
                        .reduce((left, right) -> left + PAIR_SEPARATOR + right)
                        .orElse("");

        return new MethodInfo(
                source.name(),
                source.info(),
                paramText,
                source.name() + KEY_VALUE_SEPARATOR + example);
    }

    private static String exampleValue(final String type) {
        return switch (type) {
            case "int", "Integer", "long", "Long" -> Messages.get("rx.example.int");
            case "double", "Double" -> Messages.get("rx.example.double");
            case "String" -> Messages.get("rx.example.string");
            default -> Messages.get("rx.example.default");
        };
    }
}
