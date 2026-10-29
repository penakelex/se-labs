package org.nstu.se.lab2.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nstu.se.lab2.annotations.RxMethod;
import org.nstu.se.lab2.annotations.RxParam;
import org.nstu.se.lab2.core.exceptions.ConverterException;
import org.nstu.se.lab2.core.exceptions.RxException;
import org.nstu.se.lab2.services.SumService;

import java.time.LocalDate;

class RxServiceConverterTest {
    private static final String CONVERSION_ERROR = "Нельзя преобразовать";

    private RxService service;

    @BeforeEach
    void setUp() {
        final var sum = new SumService();
        service = new RxService(sum);
        sum.configure(service);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @Test
    @DisplayName("Ошибка при отсутствии конвертера")
    void rejectsMissingConverter() {
        final var local = new RxService(new WithCustomType());
        try {
            assertThatThrownBy(() -> local.submit("methodCustom:value=2026-01-01"))
                    .isInstanceOf(RxException.class)
                    .hasMessageContaining("Нет конвертера для");
        } finally {
            local.shutdown();
        }
    }

    @Test
    @DisplayName("Ошибка при невозможности преобразовать значение")
    void rejectsInvalidConversion() {
        assertThatThrownBy(() -> service.submit("sum:inp1=abc,inp2=16,inp3=4"))
                .isInstanceOf(RxException.class)
                .hasMessageContaining(CONVERSION_ERROR);
    }

    @Test
    @DisplayName("ConverterException заворачивается в RxException")
    void wrapsConverterException() {
        service.registerConverter(
                int.class,
                value -> {
                    throw new ConverterException(new IllegalStateException("boom"));
                });
        assertThatThrownBy(() -> service.submit("sum:inp1=1,inp2=2,inp3=3"))
                .isInstanceOf(RxException.class)
                .hasMessageContaining(CONVERSION_ERROR);
    }

    static final class WithCustomType {
        @RxMethod(name = "methodCustom")
        String methodCustom(@RxParam(name = "value") final LocalDate value) {
            return value.toString();
        }
    }
}
