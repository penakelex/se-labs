package com.example;

import com.example.core.RxException;
import com.example.core.RxService;
import com.example.demo.DemoService;
import com.example.types.RxIntegerValue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {
        DemoService demo = new DemoService();
        RxService service = new RxService(demo);
        service.registerConverter(RxIntegerValue.class,
            v -> new RxIntegerValue(Integer.parseInt(v.trim())));

        try {
            // 1. Сканирование
            System.out.println("1. Сканирование");
            List<Map<String, Object>> methods = service.getAnnotatedMethods();
            System.out.println("Аннотированных методов: " + methods.size());
            for (Map<String, Object> m : methods) {
                System.out.println("  " + m.get("name") + " — " + m.get("info"));
            }
            System.out.println();

            // 2. Способы создания сервиса
            System.out.println("2. Способы создания сервиса");
            RxService s1 = new RxService(demo);
            RxService s2 = new RxService(DemoService.class);
            RxService s3 = new RxService("com.example.demo.DemoService");

            System.out.println("Object: " + s1.getAnnotatedMethods().size() + " методов");
            System.out.println("Class:  " + s2.getAnnotatedMethods().size() + " методов");
            System.out.println("String: " + s3.getAnnotatedMethods().size() + " методов");

            s1.shutdown();
            s2.shutdown();
            s3.shutdown();
            System.out.println();

            // 3. Асинхронный вызов
            System.out.println("3. Асинхронный вызов");
            System.out.println("До submit: поток " + Thread.currentThread().getName());
            Future<Object> f = service.submit("sum:inp1=15,inp2=16,inp3=4");
            System.out.println("После submit: поток " + Thread.currentThread().getName()
                + " свободен, ждём результат...");
            Object result = f.get(2, TimeUnit.SECONDS);
            System.out.println("Результат: " + result + " (метод void → null)");
            System.out.println("lastResult = " + demo.getLastResult());
            System.out.println();

            // 4. Другой формат запроса
            System.out.println("4. Другой формат запроса");
            service.submit("sum: inp3=4 , inp1=15 , inp2=16").get(2, TimeUnit.SECONDS);
            System.out.println("  пробелы + другой порядок → lastResult = " + demo.getLastResult());
            service.submit("sum:inp2=16;inp1=15;inp3=4").get(2, TimeUnit.SECONDS);
            System.out.println("  через ';'                → lastResult = " + demo.getLastResult());
            System.out.println();

            // 5. Метод не найден — синхронная ошибка
            System.out.println("5. Метод не найден");
            try {
                service.submit("unknown:inp1=1");
            } catch (RxException e) {
                System.out.println("Ожидаемая ошибка: " + e.getMessage());
            }
            System.out.println();

            // 6. Не задан параметр — синхронная ошибка
            System.out.println("6. Не задан параметр");
            try {
                service.submit("sum:inp1=15,inp2=16");
            } catch (RxException e) {
                System.out.println("Ожидаемая ошибка: " + e.getMessage());
            }
            System.out.println();

            // 7. Ошибка конвертации — синхронная
            System.out.println("7. Ошибка конвертации");
            try {
                service.submit("sum:inp1=abc,inp2=16,inp3=4");
            } catch (RxException e) {
                System.out.println("Ожидаемая ошибка: " + e.getMessage());
            }
            System.out.println();

            // 8. Ошибка внутри метода — асинхронная
            System.out.println("8. Ошибка внутри метода — асинхронная");
            try {
                service.submit("sum:inp1=-1,inp2=16,inp3=4").get(2, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                String msg = (cause.getCause() != null)
                    ? cause.getCause().getMessage()
                    : cause.getMessage();
                System.out.println("Ожидаемая ошибка: " + msg);
            }
            System.out.println();

        } finally {
            // 9. Закрытие пула — всегда, даже при исключении
            service.shutdown();
            System.out.println("Пул закрыт, программа завершается.");
        }
    }
}
