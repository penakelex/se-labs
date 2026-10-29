package org.nstu.se.lab2.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nstu.se.lab2.annotations.RxMethod;
import org.nstu.se.lab2.annotations.RxParam;
import org.nstu.se.lab2.core.exceptions.RxException;
import org.nstu.se.lab2.services.SumService;

class RxServiceScanTest {

    @Test
    @DisplayName("Сканирует аннотированные методы и отфильтровывает неаннотированные параметры")
    void scansAnnotatedMethods() {
        final var service = new RxService(new SumService());
        try {
            assertThat(service.getAnnotatedMethods())
                    .singleElement()
                    .satisfies(
                            descriptor -> {
                                assertThat(descriptor.name()).isEqualTo("sum");
                                assertThat(descriptor.parameters())
                                        .extracting(ParameterDescriptor::name)
                                        .containsExactly("inp1", "inp2", "inp3");
                            });
        } finally {
            service.shutdown();
        }
    }

    @Test
    @DisplayName("Сортирует методы по имени")
    void sortsMethodsByName() throws RxException {
        final var service = new RxService(new MultiMethod());
        try {
            assertThat(service.getAnnotatedMethods())
                    .extracting(MethodDescriptor::name)
                    .containsExactly("alpha", "beta", "gamma");
        } finally {
            service.shutdown();
        }
    }

    @Test
    @DisplayName("Создаёт сервис через объект, Class и имя класса")
    void createsFromObjectClassAndName() throws RxException {
        final var fromObject = new RxService(new SumService());
        final var fromClass = new RxService(SumService.class);
        final var fromName = new RxService(SumService.class.getName());
        try {
            assertThat(
                            java.util.List.of(
                                    fromObject.getAnnotatedMethods().size(),
                                    fromClass.getAnnotatedMethods().size(),
                                    fromName.getAnnotatedMethods().size()))
                    .containsOnly(1);
        } finally {
            fromObject.shutdown();
            fromClass.shutdown();
            fromName.shutdown();
        }
    }

    @Test
    @DisplayName("Ошибка при дублирующемся @RxMethod.name")
    void rejectsDuplicateMethodName() {
        assertThatThrownBy(() -> new RxService(new DuplicateMethods()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Дублирующееся имя @RxMethod");
    }

    @Test
    @DisplayName("Ошибка при дублирующемся @RxParam.name")
    void rejectsDuplicateParamName() {
        assertThatThrownBy(() -> new RxService(new DuplicateParams()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Дублирующееся @RxParam");
    }

    @Test
    @DisplayName("Ошибка при пустом @RxMethod.name")
    void rejectsEmptyMethodName() {
        assertThatThrownBy(() -> new RxService(new EmptyName()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пустое имя @RxMethod");
    }

    static final class MultiMethod {
        @RxMethod(name = "gamma")
        void gamma() {}

        @RxMethod(name = "alpha")
        void alpha() {}

        @RxMethod(name = "beta")
        void beta() {}
    }

    static final class DuplicateMethods {
        @RxMethod(name = "dup")
        void methodA() {}

        @RxMethod(name = "dup")
        void methodB() {}
    }

    static final class DuplicateParams {
        @RxMethod(name = "method")
        void method(@RxParam(name = "x") final int left, @RxParam(name = "x") final int right) {}
    }

    static final class EmptyName {
        @RxMethod(name = "")
        void methodEmpty() {}
    }
}
