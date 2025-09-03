package me.white.justice;

public enum HandlerType {
    FUNCTION("function", "name"),
    PROCESS("process", "name"),
    EVENT("event", "event");

    final String name;
    final String nameField;

    HandlerType(String name, String nameField) {
        this.name = name;
        this.nameField = nameField;
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

    public String getNameField() {
        return nameField;
    }
}
