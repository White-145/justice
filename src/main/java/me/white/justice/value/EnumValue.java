package me.white.justice.value;

public class EnumValue implements Value {
    private final String name;

    public EnumValue(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public ValueType getType() {
        return ValueType.ENUM;
    }

    @Override
    public String toString() {
        return "'" + name + "'";
    }
}
