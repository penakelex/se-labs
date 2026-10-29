package org.nstu.se.lab2.services;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab2.Messages;
import org.nstu.se.lab2.annotations.RxMethod;
import org.nstu.se.lab2.annotations.RxParam;
import org.nstu.se.lab2.core.RxService;
import org.nstu.se.lab2.core.convert.Converters;
import org.nstu.se.lab2.core.exceptions.RxException;

public final class SumService implements ServiceEntry {
    private @Nullable Integer lastResult;

    @Override
    public String getDisplayName() {
        return Messages.get("service.sum");
    }

    @Override
    public Object getTarget() {
        return this;
    }

    @Override
    public void configure(final RxService service) {
        Converters.registerInt(service);
        Converters.registerString(service);
    }

    @RxMethod(name = "sum", info = "output = sum of 3 inputs")
    public void sum(
            final String signature,
            @RxParam(name = "inp1") final int first,
            @RxParam(name = "inp2") final int second,
            @RxParam(name = "inp3") final int third)
            throws RxException {
        if (first < 0 || second < 0 || third < 0) {
            throw new RxException("Отрицательные входы не поддерживаются");
        }

        this.lastResult = first + second + third;
    }

    public @Nullable Integer getLastResult() {
        return lastResult;
    }
}
