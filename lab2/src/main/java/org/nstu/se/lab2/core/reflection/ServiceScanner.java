package org.nstu.se.lab2.core.reflection;

import org.nstu.se.lab2.annotations.RxMethod;
import org.nstu.se.lab2.annotations.RxParam;
import org.nstu.se.lab2.core.MethodDescriptor;
import org.nstu.se.lab2.core.ParameterDescriptor;
import org.nstu.se.lab2.core.exceptions.RxException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class ServiceScanner {
    private static final String QUOTED_IN = "' в ";
    private static final String ERROR_INSTANTIATE = "Не удалось создать экземпляр ";
    private static final String ERROR_CLASS_NOT_FOUND = "Класс не найден: ";
    private static final String ERROR_EMPTY_METHOD_NAME = "Пустое имя @RxMethod: ";
    private static final String ERROR_DUPLICATE_METHOD_NAME = "Дублирующееся имя @RxMethod: ";
    private static final String ERROR_EMPTY_PARAM_NAME = "Пустое имя @RxParam в ";
    private static final String ERROR_DUPLICATE_PARAM_NAME = "Дублирующееся @RxParam '";
    private static final String ERROR_NO_DEFAULT_CTOR = "Нет конструктора без аргументов у ";

    private final Map<String, Method> methodMap;
    private final List<MethodDescriptor> descriptorList;

    private ServiceScanner(
            final Map<String, Method> methodMap, final List<MethodDescriptor> descriptorList) {
        this.methodMap = methodMap;
        this.descriptorList = descriptorList;
    }

    public static Object instantiate(final Class<?> clazz) throws RxException {
        try {
            final var constructor = clazz.getDeclaredConstructor();
            constructor.trySetAccessible();
            return constructor.newInstance();
        } catch (final NoSuchMethodException exception) {
            throw new RxException(ERROR_NO_DEFAULT_CTOR + clazz.getName(), exception);
        } catch (final ReflectiveOperationException exception) {
            throw new RxException(ERROR_INSTANTIATE + clazz.getName(), exception);
        }
    }

    public static Object loadAndInstantiate(final String className) throws RxException {
        try {
            return instantiate(Class.forName(className));
        } catch (final ClassNotFoundException exception) {
            throw new RxException(ERROR_CLASS_NOT_FOUND + className, exception);
        }
    }

    public static ServiceScanner scan(final Class<?> clazz) {
        final var byName = new LinkedHashMap<String, Method>();
        final var found = new ArrayList<MethodDescriptor>();

        final var allMethods = new LinkedHashSet<Method>();
        allMethods.addAll(List.of(clazz.getMethods()));
        allMethods.addAll(List.of(clazz.getDeclaredMethods()));

        for (final var method : allMethods) {
            final var rxMethod = method.getAnnotation(RxMethod.class);
            if (rxMethod == null) {
                continue;
            }

            final var name = rxMethod.name();
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException(ERROR_EMPTY_METHOD_NAME + method);
            }
            if (byName.containsKey(name)) {
                throw new IllegalArgumentException(ERROR_DUPLICATE_METHOD_NAME + name);
            }

            byName.put(name, method);
            found.add(describe(method, rxMethod));
        }

        found.sort(Comparator.comparing(MethodDescriptor::name));
        return new ServiceScanner(Map.copyOf(byName), List.copyOf(found));
    }

    public Map<String, Method> methodsByName() {
        return Map.copyOf(methodMap);
    }

    public List<MethodDescriptor> descriptors() {
        return List.copyOf(descriptorList);
    }

    private static MethodDescriptor describe(final Method method, final RxMethod rxMethod) {
        return new MethodDescriptor(
                rxMethod.name(),
                rxMethod.info(),
                method.getReturnType(),
                describeParameters(method));
    }

    private static List<ParameterDescriptor> describeParameters(final Method method) {
        final var parameters = new ArrayList<ParameterDescriptor>();
        final var seenNames = new HashSet<String>();

        for (final var parameter : method.getParameters()) {
            final var rxParam = parameter.getAnnotation(RxParam.class);
            if (rxParam == null) {
                continue;
            }

            final var name = rxParam.name();
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException(ERROR_EMPTY_PARAM_NAME + method);
            }
            if (!seenNames.add(name)) {
                throw new IllegalArgumentException(
                        ERROR_DUPLICATE_PARAM_NAME + name + QUOTED_IN + method);
            }

            parameters.add(new ParameterDescriptor(name, parameter.getType()));
        }

        return List.copyOf(parameters);
    }
}
