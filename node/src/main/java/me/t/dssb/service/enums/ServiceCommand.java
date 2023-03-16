package me.t.dssb.service.enums;

public enum ServiceCommand {
    START("/start"),
    CANCEL("/cancel"),
    REGISTRATION("/registration"),
    HELP("/help");

    private final String value;

    ServiceCommand(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ServiceCommand fromValue(String value) {
        for (var c : ServiceCommand.values()) {
            if (c.value.equals(value)) {
                return c;
            }
        }
        return null;
    }

    //    public boolean equals(String value) {
//        return this.toString().equals(value);
//    }
}
