package com.console.core.command;

import com.liubs.findinstances.jvmti.InstancesOfClass;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OgnlCommand {

    private Class<?> beanFactoryClass;
    private static final Pattern letPattern = Pattern.compile("\\s*let\\s+(\\w+)\\s*=\\s*(.*)");
    private static final Pattern beanPattern = Pattern.compile("bean\\[(.*?)]");
    public static Instrumentation instrumentation;
    private static volatile OgnlCommand ognlCommand;
    private final OgnlContext context = new OgnlContext();
    private final Map<String, Object> map = new HashMap<>();

    public static OgnlCommand getInstance(Instrumentation instrumentation) {
        if (ognlCommand == null) {
            ognlCommand = new OgnlCommand(instrumentation);
        }
        return ognlCommand;
    }

    public OgnlCommand(Instrumentation instrumentation) {
        OgnlCommand.instrumentation = instrumentation;
        context.setMemberAccess(new DefaultMemberAccess(true));
        context.setClassResolver(CustomClassResolver.customClassResolver);
        Arrays.stream(instrumentation.getAllLoadedClasses())
                .filter(it -> "org.springframework.beans.factory.BeanFactory".equals(it.getName()))
                .findFirst().ifPresent(clazz -> {
                    beanFactoryClass = clazz;
                    List<Object> instanceList = InstancesOfClass.getInstanceList(clazz, 1);
                    if (!instanceList.isEmpty()) {
                        context.put("beanFactory", instanceList.get(0));
                    }
                });
    }

    public String exec(String expression) {
        Matcher beanMatcher = beanPattern.matcher(expression);
        try {
            if (beanMatcher.find()) {
                for (int i = 0; i < beanMatcher.groupCount(); i++) {
                    String beanName = beanMatcher.group(i + 1);
                    Object beanObj = map.get(beanName);
                    if (beanObj == null) {
                        try {
                            beanObj = beanFactoryClass.getDeclaredMethod("getBean", String.class).invoke(context.get("beanFactory"), beanName);
                        } catch (Exception e) {
                            beanObj = beanFactoryClass.getDeclaredMethod("getBean", String.class).invoke(context.get("beanFactory"), firstCharToLowerCase(beanName));
                        }
                        map.put(beanName, beanObj);
                    }
                    expression = expression.replace(String.format("bean[%s]", beanName), beanName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            if (e.getCause() != null) {
                return e.getCause().toString();
            } else {
                return "exception: " + e.getMessage();
            }
        }

        Matcher matcher = letPattern.matcher(expression);
        try {
            if (matcher.find() && matcher.groupCount() == 2) {
                map.put(matcher.group(1), Ognl.getValue(matcher.group(2), context, map));
                return null;
            } else {
                Object value = Ognl.getValue(expression, context, map);
                return value == null ? "null" : value.toString();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return e.getMessage();
        }
    }

    private static String firstCharToLowerCase(String beanName) {
        String lastStr = beanName.length() > 1 ? beanName.substring(1) : "";
        return Character.toLowerCase(beanName.charAt(0)) + lastStr;
    }

}
