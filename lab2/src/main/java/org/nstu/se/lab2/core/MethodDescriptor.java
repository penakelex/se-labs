package org.nstu.se.lab2.core;

import java.util.List;

public record MethodDescriptor(
        String name, String info, Class<?> returnType, List<ParameterDescriptor> parameters) {
    public MethodDescriptor {
        parameters = List.copyOf(parameters);
    }
}
