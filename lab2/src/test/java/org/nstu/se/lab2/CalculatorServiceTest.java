package org.nstu.se.lab2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.nstu.se.lab2.core.RxService;
import org.nstu.se.lab2.services.CalculatorService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

class CalculatorServiceTest {
    private RxService service;

    @BeforeEach
    void setUp() {
        final var calculator = new CalculatorService();
        service = new RxService(calculator);
        calculator.configure(service);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {"add:a=2,b=3 | 5", "sub:a=5,b=3 | 2", "mul:a=4,b=3 | 12", "div:a=12,b=3 | 4"})
    @DisplayName("Арифметические операции")
    void calculatorWorks(final String request, final int expected) throws Exception {
        assertThat(service.submit(request).get(2, TimeUnit.SECONDS)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Деление на ноль")
    void rejectsDivisionByZero() {
        assertThatThrownBy(() -> service.submit("div:a=1,b=0").get(2, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasRootCauseMessage("Деление на ноль");
    }
}
