package com.console.core.command;

import ognl.ClassResolver;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author diecui1202 on 2017/9/29.
 * @see ognl.DefaultClassResolver
 */
public class CustomClassResolver implements ClassResolver {

    public static final CustomClassResolver customClassResolver = new CustomClassResolver();

    private Map<String, Class<?>> classes = new ConcurrentHashMap<>(101);

    private CustomClassResolver() {

    }

    public Class<?> classForName(String className, Map context) throws ClassNotFoundException {
        Class<?> result;

        if ((result = classes.get(className)) == null) {
            try {
                Optional<Class> classOptional = Arrays.stream(OgnlCommand.instrumentation.getAllLoadedClasses()).filter(it -> it.getName().equals(className)).findFirst();
                if (classOptional.isPresent()) {
                    result = classOptional.get();
                }
                if (result == null) {
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    if (classLoader != null) {
                        result = classLoader.loadClass(className);
                    } else {
                        result = Class.forName(className);
                    }
                }
            } catch (ClassNotFoundException ex) {
                if (className.indexOf('.') == -1) {
                    result = Class.forName("java.lang." + className);
                    classes.put("java.lang." + className, result);
                }
            }
            classes.put(className, result);
        }
        return result;
    }
}