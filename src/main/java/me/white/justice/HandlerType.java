package me.white.justice;

public enum HandlerType {
    FUNCTION("function"),
    PROCESS("process"),
    EVENT("event");

    final String name;

    HandlerType(String name) {
        this.name = name;
    }

    public static HandlerType byName(String name) {
        for (HandlerType type : HandlerType.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
