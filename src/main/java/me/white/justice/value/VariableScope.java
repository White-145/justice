package me.white.justice.value;

public enum VariableScope {
    LOCAL('l', "local"),
    GAME('g', "game"),
    SAVE('s', "save");

    final char prefix;
    final String name;

    VariableScope(char prefix, String name) {
        this.prefix = prefix;
        this.name = name;
    }

    public static VariableScope byPrefix(char prefix) {
        for (VariableScope scope : VariableScope.values()) {
            if (scope.prefix == prefix) {
                return scope;
            }
        }
        return null;
    }

    public static VariableScope byName(String name) {
        for (VariableScope scope : VariableScope.values()) {
            if (scope.name.equals(name)) {
                return scope;
            }
        }
        return null;
    }

    public char getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }
}
