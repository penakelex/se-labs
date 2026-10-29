package org.nstu.se.lab2.ui;

import org.nstu.se.lab2.Messages;
import org.nstu.se.lab2.core.RxService;
import org.nstu.se.lab2.core.exceptions.RxException;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class CommandExecutor {
    private static final long TIMEOUT_SECONDS = 10;

    private CommandExecutor() {}

    public static Object execute(final RxService service, final String request) {
        final Future<Object> future;
        try {
            future = service.submit(request);
        } catch (final RxException exception) {
            throw new CompletionException(exception);
        }

        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (final InterruptedException exception) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new CompletionException(exception);
        } catch (final ExecutionException exception) {
            throw new CompletionException(exception);
        } catch (final TimeoutException exception) {
            future.cancel(true);
            throw new CompletionException(
                    new IllegalStateException(Messages.get("rx.error.timeout"), exception));
        }
    }

    public static Throwable unwrap(final Throwable error) {
        var current = error;

        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }

        return current;
    }

    public static String errorMessage(final Throwable cause) {
        final var message = cause.getMessage();
        return message != null ? message : cause.getClass().getSimpleName();
    }
}
