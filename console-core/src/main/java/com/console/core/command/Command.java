package com.console.core.command;

import java.lang.instrument.Instrumentation;

public abstract class Command {

    public String execute(String expression, Instrumentation instrumentation) {
//        String[] split = expression.split("\\s+");
//        if (split.length < 2) {
//            return "请输入表达式";
//        }
        return exec(expression, instrumentation);
    }

    abstract String exec(String expression, Instrumentation instrumentation);
}
