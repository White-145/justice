package me.white.justice.value;

public class NumberValue implements Value {
    private final String placeholder;
    private final double number;

    public NumberValue(String placeholder) {
        this.placeholder = placeholder;
        number = 0;
    }

    public NumberValue(double number) {
        placeholder = null;
        this.number = number;
    }

    @Override
    public ValueType getType() {
        return ValueType.NUMBER;
    }

    public boolean isPlaceholder() {
        return placeholder != null;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public double getNumber() {
        return number;
    }
}
