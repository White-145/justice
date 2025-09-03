package me.white.justice;

import me.white.justice.value.Value;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class Operation {
    private final String name;
    private final boolean isInverted;
    @Nullable
    private final String delegate;
    @Nullable
    private final String selector;
    private final Map<String, Value> arguments;
    private final List<Operation> operations;

    public Operation(String name, boolean isInverted, String delegate, String selector, Map<String, Value> arguments, List<Operation> operations) {
        this.name = name;
        this.isInverted = isInverted;
        this.delegate = delegate;
        this.selector = selector;
        this.arguments = arguments;
        this.operations = operations;
    }

    public boolean hasDelegate() {
        return delegate != null;
    }

    public boolean hasSelector() {
        return selector != null;
    }

    public String getName() {
        return name;
    }

    public boolean isInverted() {
        return isInverted;
    }

    @Nullable
    public String getDelegate() {
        return delegate;
    }

    @Nullable
    public String getSelector() {
        return selector;
    }

    public Map<String, Value> getArguments() {
        return arguments;
    }

    public List<Operation> getOperations() {
        return operations;
    }
}
