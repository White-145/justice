package me.white.justice.parser;

import java.util.List;

public class Handler {
    private final HandlerType type;
    private final String name;
    private final List<Operation> operations;

    public Handler(HandlerType type, String name, List<Operation> operations) {
        this.type = type;
        this.name = name;
        this.operations = operations;
    }

    public HandlerType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<Operation> getOperations() {
        return operations;
    }
}
