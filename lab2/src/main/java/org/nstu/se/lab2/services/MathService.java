package org.nstu.se.lab2.services;

import org.nstu.se.lab2.Messages;
import org.nstu.se.lab2.annotations.RxMethod;
import org.nstu.se.lab2.annotations.RxParam;
import org.nstu.se.lab2.core.RxService;
import org.nstu.se.lab2.core.convert.Converters;
import org.nstu.se.lab2.core.exceptions.RxInvocationException;

public final class MathService implements ServiceEntry {
    @Override
    public String getDisplayName() {
        return Messages.get("service.math");
    }

    @Override
    public Object getTarget() {
        return this;
    }

    @Override
    public void configure(final RxService service) {
        Converters.registerDouble(service);
        Converters.registerInt(service);
    }

    @RxMethod(name = "pow", info = "base ^ exponent")
    public double pow(
            @RxParam(name = "base") final double base, @RxParam(name = "exp") final double exp) {
        return Math.pow(base, exp);
    }

    @RxMethod(name = "sqrt", info = "square root")
    public double sqrt(@RxParam(name = "value") final double value) {
        if (value < 0) {
            throw new ArithmeticException("Корень из отрицательного числа");
        }
        return Math.sqrt(value);
    }

    @RxMethod(name = "sleep", info = "пауза в миллисекундах")
    public String sleep(@RxParam(name = "ms") final int millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RxInvocationException("Прервано", exception);
        }
        return "прошло " + millis + " мс";
    }
}
