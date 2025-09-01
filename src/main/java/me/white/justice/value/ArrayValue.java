package me.white.justice.value;

import java.util.ArrayList;

public class ArrayValue extends ArrayList<Value> implements Value {
    @Override
    public ValueType getType() {
        return ValueType.ARRAY;
    }
}
