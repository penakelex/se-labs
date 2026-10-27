package com.example.core;

import com.example.annotations.RxMethod;
import com.example.annotations.RxParam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class RxService {

    private final ExecutorService pool = Executors.newCachedThreadPool();

    private final Object target;

    //  «каталог» — ArrayList<HashMap>
    private final List<Map<String, Object>> annotatedMethods = new ArrayList<>();

    // быстрый поиск — имя метода -> объект Method
    private final Map<String, Method> methodsByName = new HashMap<>();

    private final Map<Class<?>, Function<String, Object>> converters = new HashMap<>();

    public RxService(Object target) {
        this.target = target;
        converters.put(int.class, s -> Integer.parseInt(s.trim()));
        converters.put(Integer.class, s -> Integer.parseInt(s.trim()));
        converters.put(long.class, s -> Long.parseLong(s.trim()));
        converters.put(String.class, s -> s);
        scan(target.getClass());
    }

    public RxService(Class<?> clazz) throws RxException {
        this(instantiate(clazz));
    }

    public RxService(String className) throws RxException {
        this(loadAndInstantiate(className));
    }

    private static Object instantiate(Class<?> clazz) throws RxException {
        try {
            var ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new RxException("Нет конструктора без аргументов у " + clazz.getName(), e);
        } catch (ReflectiveOperationException e) {
            throw new RxException("Не удалось создать экземпляр " + clazz.getName(), e);
        }
    }

    private static Object loadAndInstantiate(String className) throws RxException {
        try {
            return instantiate(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new RxException("Класс не найден: " + className, e);
        }
    }

    private void scan(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            RxMethod rxMethod = method.getAnnotation(RxMethod.class);
            if (rxMethod == null) {
                continue;
            }

            methodsByName.put(rxMethod.name(), method);

            Map<String, Object> descriptor = new HashMap<>();
            descriptor.put("name", rxMethod.name());
            descriptor.put("info", rxMethod.info());
            descriptor.put("method", method);
            descriptor.put("returnType", method.getReturnType().getSimpleName());
            descriptor.put("paramCount", method.getParameterCount());

            List<Map<String, String>> params = new ArrayList<>();
            for (Parameter p : method.getParameters()) {
                RxParam rxParam = p.getAnnotation(RxParam.class);
                Map<String, String> paramInfo = new HashMap<>();
                paramInfo.put("type", p.getType().getSimpleName());
                paramInfo.put("name", rxParam != null ? rxParam.name() : "<без @RxParam>");
                params.add(paramInfo);
            }
            descriptor.put("params", params);

            annotatedMethods.add(descriptor);
        }
    }

    public List<Map<String, Object>> getAnnotatedMethods() {
        return annotatedMethods;
    }

    public Method getMethod(String name) {
        return methodsByName.get(name);
    }

    public void registerConverter(Class<?> type, Function<String, Object> f) {
        converters.put(type, f);
    }

    // Парсинг запроса
    public RxRequest parse(String request) throws RxException {
        if (request == null || request.isBlank()) {
            throw new RxException("Пустой запрос");
        }

        String trimmed = request.trim();
        int colon = trimmed.indexOf(':');
        if (colon < 0) {
            throw new RxException("Нет ':' в запросе: " + request);
        }

        String methodName = trimmed.substring(0, colon).trim();
        if (methodName.isEmpty()) {
            throw new RxException("Пустое имя метода в запросе: " + request);
        }

        Map<String, String> params = new HashMap<>();
        String rest = trimmed.substring(colon + 1).trim();

        if (!rest.isEmpty()) {
            // split по запятой или точке с запятой — «любой формат»
            for (String pair : rest.split("[,;]")) {
                String p = pair.trim();
                if (p.isEmpty()) continue;      // "sum:," — просто игнорируем пустое

                int eq = p.indexOf('=');
                if (eq < 0) {
                    throw new RxException("Нет '=' в паре: '" + p + "'");
                }
                String key = p.substring(0, eq).trim();
                String value = p.substring(eq + 1).trim();

                if (key.isEmpty()) {
                    throw new RxException("Пустое имя параметра в паре: '" + p + "'");
                }
                if (params.containsKey(key)) {
                    throw new RxException("Дублирующийся параметр: " + key);
                }
                params.put(key, value);
            }
        }

        return new RxRequest(methodName, params);
    }

    private Object convert(Class<?> type, String raw) throws RxException {
        var f = converters.get(type);
        if (f == null) throw new RxException("Нет конвертера для " + type.getSimpleName());
        try {
            return f.apply(raw);
        } catch (RuntimeException e) {
            throw new RxException("Нельзя преобразовать '" + raw + "' в " + type.getSimpleName(), e);
        }
    }

    private Object[] buildArgs(Method method, Map<String, String> values) throws RxException {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            RxParam rxParam = parameters[i].getAnnotation(RxParam.class);
            if (rxParam == null) {
                args[i] = null;                 // signature без @RxParam
                continue;
            }
            String raw = values.get(rxParam.name());
            if (raw == null) {
                throw new RxException("Не задан параметр: " + rxParam.name());
            }
            args[i] = convert(parameters[i].getType(), raw);
        }
        return args;
    }

    public Future<Object> submit(String request) throws RxException {
        RxRequest req = parse(request);

        Method method = methodsByName.get(req.methodName());
        if (method == null) {
            throw new RxException("Метод не найден: " + req.methodName());
        }

        Object[] args = buildArgs(method, req.params());

        // Асинхронный вызов
        return pool.submit(() -> {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RxException rx) {
                    throw new RuntimeException(rx.getMessage(), rx);   // ← message отдельно
                }
                throw new RuntimeException(cause.getMessage(), cause);

            } catch (IllegalAccessException e) {
                throw new RuntimeException("Нет доступа к методу " + method.getName(), e);
            }
        });
    }

    public void shutdown() {
        pool.shutdown();
    }
}
