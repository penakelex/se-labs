package org.nstu.se.lab1.ui;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.application.Platform;

import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class JavaFxTestBase {
    private static final int TIMEOUT_SECONDS = 5;

    protected JavaFxTestBase() {}

    @BeforeAll
    static void initToolkit() throws InterruptedException {
        final var latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (final IllegalStateException exception) {
            latch.countDown();
        }

        final var isStarted = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(isStarted).isTrue();
    }

    protected static <T> T runOnFxThread(final Supplier<T> action) {
        final var holder = new FxResultHolder<>(action);
        holder.executeAndWait();
        return holder.getResult();
    }

    private static final class FxResultHolder<T> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private final Supplier<T> action;
        private T result;

        private FxResultHolder(final Supplier<T> action) {
            this.action = action;
        }

        private void executeAndWait() {
            Platform.runLater(
                    () -> {
                        try {
                            result = action.get();
                        } finally {
                            latch.countDown();
                        }
                    });

            try {
                final var finished = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                assertThat(finished).isTrue();
            } catch (final InterruptedException exception) {
                throw new IllegalStateException("FX thread execution interrupted", exception);
            }
        }

        private T getResult() {
            return result;
        }
    }
}
