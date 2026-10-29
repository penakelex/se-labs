package org.nstu.se.lab2.core;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab2.core.convert.ValueConverter;
import org.nstu.se.lab2.core.exceptions.RxException;
import org.nstu.se.lab2.core.exceptions.RxInvocationException;
import org.nstu.se.lab2.core.parsing.RequestParser;
import org.nstu.se.lab2.core.reflection.ArgumentBinder;
import org.nstu.se.lab2.core.reflection.ServiceScanner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class RxService {
    private static final String ERROR_METHOD_NOT_FOUND = "Метод не найден: ";
    private static final String ERROR_NO_ACCESS = "Нет доступа к методу ";
    private static final String ERROR_INVOCATION = "Ошибка вызова ";

    private final Object target;
    private final List<MethodDescriptor> annotatedMethods;
    private final Map<String, Method> methodsByName;
    private final Map<Class<?>, ValueConverter> converters = new ConcurrentHashMap<>();
    private final Set<PendingTask> pending = ConcurrentHashMap.newKeySet();

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public RxService(final Object target) {
        this.target = Objects.requireNonNull(target, "target");
        final var scanner = ServiceScanner.scan(target.getClass());
        this.methodsByName = scanner.methodsByName();
        this.annotatedMethods = scanner.descriptors();
    }

    public RxService(final Class<?> clazz) throws RxException {
        this(ServiceScanner.instantiate(clazz));
    }

    public RxService(final String className) throws RxException {
        this(ServiceScanner.loadAndInstantiate(className));
    }

    public List<MethodDescriptor> getAnnotatedMethods() {
        return List.copyOf(annotatedMethods);
    }

    public @Nullable Method getMethod(final String name) {
        return methodsByName.get(name);
    }

    public void registerConverter(final Class<?> type, final ValueConverter converter) {
        converters.put(Objects.requireNonNull(type), Objects.requireNonNull(converter));
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    public Future<Object> submit(final String request) throws RxException {
        final var rxRequest = RequestParser.parse(request);
        final var method = getMethod(rxRequest.methodName());

        if (method == null) {
            throw new RxException(ERROR_METHOD_NOT_FOUND + rxRequest.methodName());
        }

        final var args = ArgumentBinder.bind(method, rxRequest.parameters(), converters);
        final var future = CompletableFuture.supplyAsync(() -> invoke(method, args));
        final var task = new PendingTask(future);

        pending.add(task);
        future.whenComplete((_, _) -> pending.remove(task));

        return future;
    }

    private Object invoke(final Method method, final @Nullable Object... args) {
        method.trySetAccessible();
        try {
            return method.invoke(target, args);
        } catch (final InvocationTargetException exception) {
            final var cause = exception.getCause();
            final var actual = cause != null ? cause : exception;
            throw new RxInvocationException(
                    ERROR_INVOCATION + method.getName() + ": " + actual.getMessage(), actual);
        } catch (final IllegalAccessException exception) {
            throw new RxInvocationException(ERROR_NO_ACCESS + method.getName(), exception);
        }
    }

    public void shutdown() {
        for (final var task : pending) {
            task.cancel();
        }
        pending.clear();
        executor.shutdownNow();
    }

    private record PendingTask(CompletableFuture<Object> future) {
        private void cancel() {
            future.cancel(true);
        }
    }
}
