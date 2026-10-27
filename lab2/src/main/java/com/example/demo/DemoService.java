package com.example.demo;

import com.example.annotations.RxMethod;
import com.example.annotations.RxParam;
import com.example.core.RxException;
import com.example.types.RxIntegerValue;

public class DemoService {
    private Integer lastResult;

    @RxMethod(name = "sum", info = "output = sum of 3 inputs")
    public void sum(String signature,
                    @RxParam(name = "inp1") RxIntegerValue a,
                    @RxParam(name = "inp2") RxIntegerValue b,
                    @RxParam(name = "inp3") RxIntegerValue c) throws RxException {
        if (a.get() < 0 || b.get() < 0 || c.get() < 0) {
            throw new RxException("Отрицательные входы не поддерживаются");
        }
        this.lastResult = a.get() + b.get() + c.get();
    }

    public Integer getLastResult() {
        return lastResult;
    }
}
