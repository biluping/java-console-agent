package com.console.agent;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.console.agent.domain.ExecAO;
import com.liubs.findinstances.jvmti.InstancesOfClass;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaConsoleAgent {

    private static final Map<String, Class<?>> classMap = new HashMap<>();
    private static Instrumentation instrumentation;
    private static final Pattern pattern = Pattern.compile("\\s*def\\s+(\\w+)\\s*=\\s*(.*)");
    private static Binding binding = null;
    private static GroovyShell groovyShell = null;
    private static Javalin app = null;

    private static void initGroovyShell() {
        try {
            // 定义一个函数
            Closure<Object[]> getFunc = new Closure<Object[]>(null) {
                public Object[] doCall(String clazzName) {
                    Class<?> clazz = classMap.get(clazzName);
                    if (clazz == null) {
                        return new Object[0];
                    }
                    return InstancesOfClass.getInstances(clazz);
                }
            };
            binding = new Binding();
            binding.setVariable("get", getFunc);
            groovyShell = new GroovyShell(binding);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void agentmain(String args, Instrumentation inst) {
        System.out.println(ClassLoader.getSystemClassLoader());
        if (instrumentation != null) {
            System.out.println("Already attached before");
            return;
        }
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            classMap.put(clazz.getName(), clazz);
        }
        instrumentation = inst;
        initGroovyShell();
        app = Javalin.create()
                .post("/", JavaConsoleAgent::exec)
                .get("/quit", ctx -> new Thread(() -> {
                    ThreadUtil.sleep(1, TimeUnit.SECONDS);
                    app.stop();
                    initGroovyShell();
                    instrumentation = null;
                }).start()).start(7070);
    }

    private static void exec(Context context) {
        ExecAO execAO = context.bodyAsClass(ExecAO.class);
        System.out.println("script: " + execAO.getScript());
        Matcher matcher = pattern.matcher(execAO.getScript());

        try {
            if (matcher.find() && matcher.groupCount() == 2) {
                binding.setVariable(matcher.group(1), groovyShell.evaluate(matcher.group(2)));
                System.out.println("add variable: " + matcher.group(1));
            } else {
                Object eval = groovyShell.evaluate(execAO.getScript());
                if (eval != null) {
                    context.result(eval.toString());
                }
            }
        } catch (Exception e) {
            context.result(ExceptionUtil.stacktraceToString(e));
        }
    }

}
