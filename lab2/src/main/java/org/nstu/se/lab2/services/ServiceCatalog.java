package org.nstu.se.lab2.services;

import java.util.List;

public final class ServiceCatalog {
    private ServiceCatalog() {}

    public static List<ServiceEntry> entries() {
        return List.of(
                new SumService(), new CalculatorService(), new TextService(), new MathService());
    }
}
