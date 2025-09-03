package me.white.justice;

import java.util.List;

public class Handler {
    private final String name;
    private final HandlerType type;
    private final List<Operation> operations;

    public Handler(String name, HandlerType type, List<Operation> operations) {
        this.name = name;
        this.type = type;
        this.operations = operations;
    }

    public String getName() {
        return name;
    }

    public HandlerType getType() {
        return type;
    }

    public List<Operation> getOperations() {
        return operations;
    }
}
