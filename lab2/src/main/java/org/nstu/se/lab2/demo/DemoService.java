package org.nstu.se.lab2.demo;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab2.annotations.RxMethod;
import org.nstu.se.lab2.annotations.RxParam;
import org.nstu.se.lab2.core.RxException;
import org.nstu.se.lab2.types.RxIntegerValue;

public final class DemoService {
    private @Nullable Integer lastResult;

    @RxMethod(name = "sum", info = "output = sum of 3 inputs")
    public void sum(
            final String signature,
            @RxParam(name = "inp1") final RxIntegerValue first,
            @RxParam(name = "inp2") final RxIntegerValue second,
            @RxParam(name = "inp3") final RxIntegerValue third)
            throws RxException {
        if (first.get() < 0 || second.get() < 0 || third.get() < 0) {
            throw new RxException("Отрицательные входы не поддерживаются");
        }

        this.lastResult = first.get() + second.get() + third.get();
    }

    public @Nullable Integer getLastResult() {
        return lastResult;
    }
}
