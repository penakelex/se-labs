package org.nstu.se.lab2.core;

import java.util.Map;
import java.util.Objects;

public record RxRequest(String methodName, Map<String, String> parameters) {
    public RxRequest {
        Objects.requireNonNull(methodName, "methodName");
        parameters = Map.copyOf(Objects.requireNonNull(parameters, "parameters"));
    }
}
