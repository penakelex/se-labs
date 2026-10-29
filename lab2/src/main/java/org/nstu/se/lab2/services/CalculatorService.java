package org.nstu.se.lab2.services;

import org.nstu.se.lab2.Messages;
import org.nstu.se.lab2.annotations.RxMethod;
import org.nstu.se.lab2.annotations.RxParam;
import org.nstu.se.lab2.core.RxService;
import org.nstu.se.lab2.core.convert.Converters;

public final class CalculatorService implements ServiceEntry {
    @Override
    public String getDisplayName() {
        return Messages.get("service.calculator");
    }

    @Override
    public Object getTarget() {
        return this;
    }

    @Override
    public void configure(final RxService service) {
        Converters.registerInt(service);
    }

    @RxMethod(name = "add", info = "a + b")
    public int add(@RxParam(name = "a") final int left, @RxParam(name = "b") final int right) {
        return left + right;
    }

    @RxMethod(name = "sub", info = "a - b")
    public int sub(@RxParam(name = "a") final int left, @RxParam(name = "b") final int right) {
        return left - right;
    }

    @RxMethod(name = "mul", info = "a * b")
    public int mul(@RxParam(name = "a") final int left, @RxParam(name = "b") final int right) {
        return left * right;
    }

    @RxMethod(name = "div", info = "a / b")
    public int div(@RxParam(name = "a") final int left, @RxParam(name = "b") final int right) {
        if (right == 0) {
            throw new ArithmeticException("Деление на ноль");
        }
        return left / right;
    }
}
