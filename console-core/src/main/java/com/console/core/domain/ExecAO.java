package com.console.core.domain;

public class ExecAO {

    public ExecAO() {}

    public ExecAO(String script) {
        this.script = script;
    }

    private String script;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
