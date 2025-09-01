package me.white.justice.parser;

import me.white.justice.value.Value;

import java.util.List;
import java.util.Map;

public class Operation {
    private final boolean isInverted;
    private final String name;
    private final String delegate;
    private final String selector;
    private final Map<String, Value> arguments;
    private final List<Operation> operations;

    public Operation(boolean isInverted, String name, String selector, Map<String, Value> arguments, List<Operation> operations, String delegate) {
        this.isInverted = isInverted;
        this.name = name;
        this.selector = selector;
        this.arguments = arguments;
        this.operations = operations;
        this.delegate = delegate;
    }

    public String getName() {
        return name;
    }

    public boolean hasSelector() {
        return selector != null;
    }

    public String getSelector() {
        return selector;
    }

    public Map<String, Value> getArguments() {
        return arguments;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public boolean hasDelegate() {
        return delegate != null;
    }

    public String getDelegate() {
        return delegate;
    }

    public boolean isInverted() {
        return isInverted;
    }
}
