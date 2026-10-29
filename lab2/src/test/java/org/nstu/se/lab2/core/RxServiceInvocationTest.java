package org.nstu.se.lab2.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nstu.se.lab2.annotations.RxMethod;
import org.nstu.se.lab2.annotations.RxParam;
import org.nstu.se.lab2.services.SumService;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class RxServiceInvocationTest {
    private static final long TIMEOUT_SECONDS = 2;
    private static final int EXPECTED_SUM = 35;
    private static final int SLEEP_MILLIS = 5_000;
    private static final String SLOW_REQUEST_PREFIX = "slow:millis=";
    private static final String OK_RESULT = "ok";

    private SumService sumService;
    private RxService service;

    @BeforeEach
    void setUp() {
        sumService = new SumService();
        service = new RxService(sumService);
        sumService.configure(service);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @Test
    @DisplayName("Асинхронно вызывает аннотированный метод")
    void invokesAsynchronously() throws Exception {
        final var future = service.submit("sum:inp1=15,inp2=16,inp3=4");
        await(future);
        assertThat(sumService.getLastResult()).isEqualTo(EXPECTED_SUM);
    }

    @Test
    @DisplayName("Ошибка внутри метода приходит через Future")
    void propagatesMethodError() throws Exception {
        final Future<Object> future = service.submit("sum:inp1=-1,inp2=16,inp3=4");
        assertThatThrownBy(() -> await(future))
                .isInstanceOf(ExecutionException.class)
                .hasRootCauseMessage("Отрицательные входы не поддерживаются");
    }

    @Test
    @DisplayName("Неаннотированные параметры получают значения по умолчанию")
    void unannotatedParamsGetDefaults() throws Exception {
        final var local = new RxService(new WithUnannotated());
        local.registerConverter(int.class, Integer::parseInt);
        try {
            assertThat(local.submit("info:value=5").get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                    .isEqualTo("5|0|null|false");
        } finally {
            local.shutdown();
        }
    }

    @Test
    @DisplayName("submit возвращает future немедленно")
    void submitReturnsImmediately() throws Exception {
        final var slow = new SlowService();
        final var local = new RxService(slow);
        local.registerConverter(int.class, Integer::parseInt);
        try {
            final var future = local.submit(SLOW_REQUEST_PREFIX + SLEEP_MILLIS);
            slow.started().await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertThat(future.isDone()).isFalse();
        } finally {
            slow.release().countDown();
            local.shutdown();
        }
    }

    @Test
    @DisplayName("submit завершается после release")
    void submitCompletesAfterRelease() throws Exception {
        final var slow = new SlowService();
        final var local = new RxService(slow);
        local.registerConverter(int.class, Integer::parseInt);
        try {
            final var future = local.submit(SLOW_REQUEST_PREFIX + SLEEP_MILLIS);
            slow.started().await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            slow.release().countDown();
            assertThat(future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEqualTo(OK_RESULT);
        } finally {
            local.shutdown();
        }
    }

    @Test
    @DisplayName("Вызывает static-аннотированный метод")
    void invokesStaticMethod() throws Exception {
        final var local = new RxService(StaticService.class);
        local.registerConverter(int.class, Integer::parseInt);

        try {
            assertThat(local.submit("twice:value=21").get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                    .isEqualTo(42);
        } finally {
            local.shutdown();
        }
    }

    private static Object await(final Future<Object> future)
            throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("shutdown отменяет незавершённые задачи")
    void shutdownCancelsPendingTasks() throws Exception {
        final var slow = new SlowService();
        final var local = new RxService(slow);
        local.registerConverter(int.class, Integer::parseInt);

        try {
            final var future = local.submit(SLOW_REQUEST_PREFIX + SLEEP_MILLIS);
            slow.started().await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            local.shutdown();

            assertThatThrownBy(() -> future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                    .isInstanceOf(CancellationException.class);
        } finally {
            slow.release().countDown();
            local.shutdown();
        }
    }

    static final class WithUnannotated {
        private static final String PIPE = "|";

        @RxMethod(name = "info")
        String info(
                @RxParam(name = "value") final int value,
                final int zero,
                final String nil,
                final boolean flag) {
            return value + PIPE + zero + PIPE + nil + PIPE + flag;
        }
    }

    static final class SlowService {
        private final CountDownLatch startedLatch = new CountDownLatch(1);
        private final CountDownLatch releaseLatch = new CountDownLatch(1);

        CountDownLatch started() {
            return startedLatch;
        }

        CountDownLatch release() {
            return releaseLatch;
        }

        @RxMethod(name = "slow")
        String slow(@RxParam(name = "millis") final int millis) throws InterruptedException {
            startedLatch.countDown();
            releaseLatch.await(millis, TimeUnit.MILLISECONDS);
            return OK_RESULT;
        }
    }

    static final class StaticService {
        @RxMethod(name = "twice")
        static int twice(@RxParam(name = "value") final int value) {
            return value * 2;
        }
    }
}
