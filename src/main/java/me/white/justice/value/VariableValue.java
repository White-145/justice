package me.white.justice.value;

public class VariableValue implements Value {
    private final VariableScope scope;
    private final String name;

    public VariableValue(VariableScope scope, String name) {
        this.scope = scope;
        this.name = name;
    }

    public VariableScope getScope() {
        return scope;
    }

    public String getName() {
        return name;
    }

    @Override
    public ValueType getType() {
        return ValueType.VARIABLE;
    }
}
