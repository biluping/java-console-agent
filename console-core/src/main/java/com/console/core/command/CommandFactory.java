package com.console.core.command;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {

    private static final Map<String, Command> map = new HashMap<>();

    static {
        map.put("ognl", new OgnlCommand());
    }

    public static Command getCommand(String expression) {
        String command = expression.split("\\s+")[0];
        return map.get(command);
    }

}
