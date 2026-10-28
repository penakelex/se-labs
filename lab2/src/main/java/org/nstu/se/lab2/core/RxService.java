package org.nstu.se.lab2.core;

import org.jspecify.annotations.Nullable;
import org.nstu.se.lab2.annotations.RxMethod;
import org.nstu.se.lab2.annotations.RxParam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.regex.Pattern;

@SuppressWarnings({"NullAway", "PMD", "StringSplitter"})
public final class RxService {
    private static final String KEY_NAME = "name";
    private static final String KEY_INFO = "info";
    private static final String KEY_METHOD = "method";
    private static final String KEY_RETURN_TYPE = "returnType";
    private static final String KEY_PARAMETER_COUNT = "parameterCount";
    private static final String KEY_PARAMETERS = "parameters";
    private static final String KEY_TYPE = "type";
    private static final String NO_RX_PARAMETER = "<без @RxParam>";
    private static final Pattern PARAMETER_SEPARATOR = Pattern.compile("[,;]");
    private static final String CLOSING_QUOTE = "'";

    private static final String ERROR_EMPTY_REQUEST = "Пустой запрос";
    private static final String ERROR_NO_COLON = "Нет ':' в запросе: ";
    private static final String ERROR_EMPTY_METHOD = "Пустое имя метода в запросе: ";
    private static final String ERROR_NO_EQUALS = "Нет '=' в паре: '";
    private static final String ERROR_EMPTY_PARAMETER = "Пустое имя параметра в паре: '";
    private static final String ERROR_DUPLICATE_PARAMETER = "Дублирующийся параметр: ";
    private static final String ERROR_NO_CONVERTER = "Нет конвертера для ";
    private static final String ERROR_CONVERT = "Нельзя преобразовать '";
    private static final String ERROR_METHOD_NOT_FOUND = "Метод не найден: ";
    private static final String ERROR_PARAMETER_MISSING = "Не задан параметр: ";
    private static final String ERROR_NO_DEFAULT_CTOR = "Нет конструктора без аргументов у ";
    private static final String ERROR_INSTANTIATE = "Не удалось создать экземпляр ";
    private static final String ERROR_CLASS_NOT_FOUND = "Класс не найден: ";
    private static final String ERROR_NO_ACCESS = "Нет доступа к методу ";

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Object target;
    private final List<Map<String, Object>> annotatedMethods = new ArrayList<>();
    private final Map<String, Method> methodsByName = new HashMap<>();
    private final Map<Class<?>, Function<String, Object>> converters = new HashMap<>();

    public RxService(final Object target) {
        this.target = Objects.requireNonNull(target, "target");
        registerDefaultConverters();
        scan(target.getClass());
    }

    public RxService(final Class<?> clazz) throws RxException {
        this(instantiate(clazz));
    }

    public RxService(final String className) throws RxException {
        this(loadAndInstantiate(className));
    }

    private void registerDefaultConverters() {
        registerConverter(int.class, RxService::parseInt);
        registerConverter(Integer.class, RxService::parseInt);
        registerConverter(long.class, RxService::parseLong);
        registerConverter(String.class, value -> value);
    }

    private static Integer parseInt(final String value) {
        return Integer.parseInt(value.trim());
    }

    private static Long parseLong(final String value) {
        return Long.parseLong(value.trim());
    }

    private static Object instantiate(final Class<?> clazz) throws RxException {
        try {
            final var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (final NoSuchMethodException exception) {
            throw new RxException(ERROR_NO_DEFAULT_CTOR + clazz.getName(), exception);
        } catch (final ReflectiveOperationException exception) {
            throw new RxException(ERROR_INSTANTIATE + clazz.getName(), exception);
        }
    }

    private static Object loadAndInstantiate(final String className) throws RxException {
        try {
            return instantiate(Class.forName(className));
        } catch (final ClassNotFoundException exception) {
            throw new RxException(ERROR_CLASS_NOT_FOUND + className, exception);
        }
    }

    private void scan(final Class<?> clazz) {
        for (final var method : clazz.getDeclaredMethods()) {
            final var rxMethod = method.getAnnotation(RxMethod.class);
            if (rxMethod == null) {
                continue;
            }

            methodsByName.put(rxMethod.name(), method);
            annotatedMethods.add(describe(method, rxMethod));
        }
    }

    private Map<String, Object> describe(final Method method, final RxMethod rxMethod) {
        final var descriptor = new HashMap<String, Object>();
        descriptor.put(KEY_NAME, rxMethod.name());
        descriptor.put(KEY_INFO, rxMethod.info());
        descriptor.put(KEY_METHOD, method);
        descriptor.put(KEY_RETURN_TYPE, method.getReturnType().getSimpleName());
        descriptor.put(KEY_PARAMETER_COUNT, method.getParameterCount());
        descriptor.put(KEY_PARAMETERS, describeParameters(method));
        return Map.copyOf(descriptor);
    }

    private List<Map<String, String>> describeParameters(final Method method) {
        final var parameters = new ArrayList<Map<String, String>>();

        for (final var parameter : method.getParameters()) {
            final var rxParameter = parameter.getAnnotation(RxParam.class);
            final var info = new HashMap<String, String>();
            info.put(KEY_TYPE, parameter.getType().getSimpleName());
            info.put(KEY_NAME, rxParameter == null ? NO_RX_PARAMETER : rxParameter.name());
            parameters.add(Map.copyOf(info));
        }

        return List.copyOf(parameters);
    }

    public List<Map<String, Object>> getAnnotatedMethods() {
        return List.copyOf(annotatedMethods);
    }

    public @Nullable Method getMethod(final String name) {
        return methodsByName.get(name);
    }

    public void registerConverter(final Class<?> type, final Function<String, Object> converter) {
        converters.put(Objects.requireNonNull(type), Objects.requireNonNull(converter));
    }

    public RxRequest parse(final String request) throws RxException {
        if (request == null || request.isBlank()) {
            throw new RxException(ERROR_EMPTY_REQUEST);
        }

        final var trimmed = request.trim();
        final var colon = trimmed.indexOf(':');
        if (colon < 0) {
            throw new RxException(ERROR_NO_COLON + request);
        }

        final var methodName = trimmed.substring(0, colon).trim();
        if (methodName.isEmpty()) {
            throw new RxException(ERROR_EMPTY_METHOD + request);
        }

        final var rest = trimmed.substring(colon + 1).trim();
        return new RxRequest(methodName, parseParameters(rest));
    }

    private Map<String, String> parseParameters(final String rest) throws RxException {
        final var parameters = new HashMap<String, String>();

        if (rest.isEmpty()) {
            return parameters;
        }

        for (final var part : PARAMETER_SEPARATOR.split(rest)) {
            final var trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            final var keyValue = splitPair(trimmed);
            putParameter(parameters, keyValue[0], keyValue[1]);
        }

        return parameters;
    }

    private String[] splitPair(final String pair) throws RxException {
        final var equalsIndex = pair.indexOf('=');
        if (equalsIndex < 0) {
            throw new RxException(ERROR_NO_EQUALS + pair + CLOSING_QUOTE);
        }

        final var key = pair.substring(0, equalsIndex).trim();
        final var value = pair.substring(equalsIndex + 1).trim();

        if (key.isEmpty()) {
            throw new RxException(ERROR_EMPTY_PARAMETER + pair + CLOSING_QUOTE);
        }

        return new String[] {key, value};
    }

    private void putParameter(
            final Map<String, String> parameters, final String key, final String value)
            throws RxException {
        if (parameters.containsKey(key)) {
            throw new RxException(ERROR_DUPLICATE_PARAMETER + key);
        }

        parameters.put(key, value);
    }

    private Object convert(final Class<?> type, final String raw) throws RxException {
        final var converter = converters.get(type);

        if (converter == null) {
            throw new RxException(ERROR_NO_CONVERTER + type.getSimpleName());
        }

        try {
            return converter.apply(raw);
        } catch (final NumberFormatException exception) {
            throw new RxException(ERROR_CONVERT + raw + "' в " + type.getSimpleName(), exception);
        }
    }

    private Object[] buildArgs(final Method method, final Map<String, String> values)
            throws RxException {
        final var parameters = method.getParameters();
        final var args = new Object[parameters.length];

        for (var index = 0; index < parameters.length; index++) {
            final var rxParameter = parameters[index].getAnnotation(RxParam.class);

            if (rxParameter == null) {
                args[index] = null;
                continue;
            }

            final var raw = values.get(rxParameter.name());
            if (raw == null) {
                throw new RxException(ERROR_PARAMETER_MISSING + rxParameter.name());
            }

            args[index] = convert(parameters[index].getType(), raw);
        }

        return args;
    }

    public Future<Object> submit(final String request) throws RxException {
        final var rxRequest = parse(request);
        final var method = getMethod(rxRequest.methodName());

        if (method == null) {
            throw new RxException(ERROR_METHOD_NOT_FOUND + rxRequest.methodName());
        }

        final var args = buildArgs(method, rxRequest.parameters());
        return pool.submit(() -> invoke(method, args));
    }

    private Object invoke(final Method method, final Object[] args) {
        try {
            return method.invoke(target, args);
        } catch (final InvocationTargetException exception) {
            final var cause = exception.getCause();
            final var message = cause == null ? exception.getMessage() : cause.getMessage();
            throw new RxInvocationException(message, cause);
        } catch (final IllegalAccessException exception) {
            throw new RxInvocationException(ERROR_NO_ACCESS + method.getName(), exception);
        }
    }

    public void shutdown() {
        pool.shutdown();
    }
}
