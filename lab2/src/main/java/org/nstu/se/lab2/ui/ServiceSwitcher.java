package org.nstu.se.lab2.ui;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab2.core.RxService;
import org.nstu.se.lab2.services.ServiceEntry;

import java.util.List;
import java.util.function.Consumer;

final class ServiceSwitcher {
    private final Consumer<Throwable> errorHandler;

    ServiceSwitcher(final Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Nullable Result switchTo(@Nullable final ServiceEntry entry) {
        if (entry == null) {
            return null;
        }

        final var newService = createService(entry);
        if (newService == null) {
            return null;
        }

        final var methods = configureAndScan(newService, entry);
        if (methods.isEmpty()) {
            newService.shutdown();
            return null;
        }

        return new Result(newService, methods);
    }

    private @Nullable RxService createService(final ServiceEntry entry) {
        try {
            return new RxService(entry.getTarget());
        } catch (final IllegalArgumentException exception) {
            errorHandler.accept(exception);
            return null;
        }
    }

    private List<MethodInfo> configureAndScan(final RxService service, final ServiceEntry entry) {
        try {
            entry.configure(service);
            return service.getAnnotatedMethods().stream().map(MethodInfo::from).toList();
        } catch (final IllegalArgumentException | IllegalStateException exception) {
            errorHandler.accept(exception);
            return List.of();
        }
    }

    record Result(RxService service, List<MethodInfo> methods) {}
}
