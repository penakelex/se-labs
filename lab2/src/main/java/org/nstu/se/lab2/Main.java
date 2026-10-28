package org.nstu.se.lab2;

import org.nstu.se.lab2.core.RxService;
import org.nstu.se.lab2.demo.DemoService;
import org.nstu.se.lab2.types.RxIntegerValue;

import java.util.concurrent.TimeUnit;

@SuppressWarnings({"PMD.SystemPrintln", "PMD.SignatureDeclareThrowsException"})
public final class Main {
    private static final int TIMEOUT_SECONDS = 2;
    private static final String REQUEST = "sum:inp1=15,inp2=16,inp3=4";

    private Main() {}

    static void main() throws Exception {
        final var demo = new DemoService();
        final var service = new RxService(demo);

        try {
            service.registerConverter(
                    RxIntegerValue.class,
                    value -> new RxIntegerValue(Integer.parseInt(value.trim())));

            final var future = service.submit(REQUEST);
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            final var result = demo.getLastResult();
            final var display = result == null ? "null" : String.valueOf(result);
            System.out.println("lastResult=" + display);
        } finally {
            service.shutdown();
        }
    }
}
