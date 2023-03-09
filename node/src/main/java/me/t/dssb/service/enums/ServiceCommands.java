package me.t.dssb.service.enums;

public enum ServiceCommands {
    START("/start"),
    CANCEL("/cancel"),
    REGISTRATION("/registration"),
    HELP("/help");

    private final String cmd;

    ServiceCommands(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        return cmd;
    }

    public boolean equals(String cmd) {
        return this.toString().equals(cmd);
    }
}
