package com.console.core.command;

import com.liubs.findinstances.jvmti.InstancesOfClass;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;

public class OgnlCommand extends Command {

    private OgnlContext context;
    private OgnlRoot ognlRoot;

    public OgnlCommand() {
        context = new OgnlContext();
        context.setMemberAccess(new DefaultMemberAccess(true));
    }

    public String exec(String expression, Instrumentation instrumentation) {
        if (ognlRoot == null) {
            ognlRoot = new OgnlRoot(instrumentation);
        }
        try {
            Object obj = Ognl.getValue(expression, context, ognlRoot);
            return obj == null ? null : obj.toString();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    private static class OgnlRoot {

        private Object spring;

        public OgnlRoot(Instrumentation instrumentation) {
            Arrays.stream(instrumentation.getAllLoadedClasses())
                    .filter(it -> "org.springframework.context.ApplicationContext".equals(it.getName()))
                    .findFirst().ifPresent(appClass -> {
                        List<Object> instanceList = InstancesOfClass.getInstanceList(appClass, 1);
                        if (!instanceList.isEmpty()) {
                            spring = instanceList.get(0);
                        }
                    });

        }
    }

}
