package com.console.core.command;

import com.liubs.findinstances.jvmti.InstancesOfClass;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OgnlCommand {

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
                .filter(it -> "org.springframework.context.ApplicationContext".equals(it.getName()))
                .findFirst().ifPresent(appClass -> {
                    List<Object> instanceList = InstancesOfClass.getInstanceList(appClass, 1);
                    if (!instanceList.isEmpty()) {
                        context.put("spring", instanceList.get(0));
                    }
                });
    }

    public String exec(String expression) throws OgnlException {
        Object obj = Ognl.getValue(expression, context, map);
        return obj == null ? null : obj.toString();
    }

}
