package org.nstu.se.lab2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.nstu.se.lab2.core.RxService;
import org.nstu.se.lab2.services.MathService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

class MathServiceTest {
    private RxService service;

    @BeforeEach
    void setUp() {
        final var math = new MathService();
        service = new RxService(math);
        math.configure(service);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {"pow:base=2,exp=10 | 1024.0", "sqrt:value=9 | 3.0"})
    void mathWorks(final String request, final double expected) throws Exception {
        assertThat(service.submit(request).get(2, TimeUnit.SECONDS)).isEqualTo(expected);
    }

    @Test
    void rejectsNegativeSqrt() {
        assertThatThrownBy(() -> service.submit("sqrt:value=-1").get(2, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasRootCauseMessage("Корень из отрицательного числа");
    }
}
