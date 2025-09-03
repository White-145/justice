package me.white.justice;

import com.google.gson.stream.JsonWriter;
import me.white.justice.lexer.Lexer;
import me.white.justice.lexer.Token;
import me.white.justice.lexer.TokenType;
import me.white.justice.value.*;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private final List<Handler> handlers = new ArrayList<>();
    private boolean isCompact = false;

    public Parser() { }

    public boolean isCompact() {
        return isCompact;
    }

    public void setCompact(boolean compact) {
        isCompact = compact;
    }

    public void parse(String source) throws ParsingException {
        Lexer lexer = new Lexer(source);
        while (lexer.hasNext()) {
            handlers.add(parseHandler(lexer));
        }
    }

    private static Handler parseHandler(Lexer lexer) throws ParsingException {
        List<Operation> operations = new ArrayList<>();
        HandlerType type = HandlerType.FUNCTION;
        Token name = lexer.expectIdentifier();
        if (!name.isOf(TokenType.IDENTIFIER) && lexer.peek().getType().isIdentifier()) {
            HandlerType constitute = HandlerType.byName(name.getString());
            if (constitute != null) {
                type = constitute;
                name = lexer.expectIdentifier();
            }
        }
        lexer.expect(TokenType.BLOCK_OPEN);
        while (lexer.hasNext() && !lexer.peek().isOf(TokenType.BLOCK_CLOSE)) {
            operations.add(parseOperation(lexer));
        }
        lexer.expect(TokenType.BLOCK_CLOSE);
        return new Handler(name.getString(), type, operations);
    }

    private static Operation parseOperation(Lexer lexer) throws ParsingException {
        boolean isInverted = false;
        Token name = lexer.expectIdentifier();
        Token delegate = null;
        String selector = null;
        Map<String, Value> arguments = new HashMap<>();
        List<Operation> operations = new ArrayList<>();
        if (lexer.peek().getType().isIdentifier()) {
            delegate = lexer.expectIdentifier();
        }
        if (name.isOf(TokenType.LITERAL) && name.getString().equals("not")) {
            isInverted = true;
            if (delegate == null) {
                throw new ParsingException("Inversion without an action");
            }
            name = delegate;
            delegate = null;
        } else if (delegate != null && delegate.isOf(TokenType.LITERAL) && delegate.getString().equals("not")) {
            isInverted = true;
            delegate = lexer.expectIdentifier();
        }
        if (lexer.peek().isOf(TokenType.SELECTOR_OPEN)) {
            lexer.read();
            selector = lexer.expect(TokenType.LITERAL).getString();
            lexer.expect(TokenType.SELECTOR_CLOSE);
        }
        if (lexer.peek().isOf(TokenType.ARGS_OPEN)) {
            lexer.read();
            while (lexer.hasNext() && !lexer.peek().isOf(TokenType.ARGS_CLOSE)) {
                String argument = lexer.expect(TokenType.LITERAL).getString();
                lexer.expect(TokenType.EQUALS);
                Value value = parseValue(lexer, true);
                arguments.put(argument, value);
                if (!lexer.peek().isOf(TokenType.ARGS_CLOSE)) {
                    lexer.expect(TokenType.COMMA);
                }
            }
            lexer.expect(TokenType.ARGS_CLOSE);
        }
        if (lexer.peek().isOf(TokenType.BLOCK_OPEN)) {
            lexer.read();
            while (lexer.hasNext() && !lexer.peek().isOf(TokenType.BLOCK_CLOSE)) {
                operations.add(parseOperation(lexer));
            }
            lexer.expect(TokenType.BLOCK_CLOSE);
        } else {
            lexer.expect(TokenType.EOL);
        }
        return new Operation(name.getString(), isInverted, delegate == null ? null : delegate.getString(), selector, arguments, operations);
    }

    private static Value parseValue(Lexer lexer, boolean allowLists) throws ParsingException {
        Token token = lexer.read();
        return switch (token.getType()) {
            case LITERAL -> {
                String name = token.getString();
                if (name.length() == 1) {
                    char prefix = name.charAt(0);
                    Token next = lexer.peek();
                    if (next.isOf(TokenType.STRING)) {
                        lexer.read();
                        TextParsing parsing = TextParsing.byPrefix(prefix);
                        if (parsing == null) {
                            throw new ParsingException("Invalid text parsing '" + prefix + "'");
                        }
                        yield new TextValue(next.getString(), parsing);
                    }
                    if (next.isOf(TokenType.IDENTIFIER)) {
                        lexer.read();
                        VariableScope scope = VariableScope.byPrefix(prefix);
                        if (scope == null) {
                            throw new ParsingException("Invalid variable scope '" + prefix + "'");
                        }
                        yield new VariableValue(next.getString(), scope);
                    }
                }
                ValueType type = ValueType.byName(name);
                if (type != null && type.isFactory() && lexer.peek().isOf(TokenType.BLOCK_OPEN)) {
                    yield type.parse(lexer);
                }
                yield new VariableValue(name, VariableScope.LOCAL);
            }
            case IDENTIFIER -> new VariableValue(token.getString(), VariableScope.LOCAL);
            case STRING -> new TextValue(token.getString(), TextParsing.PLAIN);
            case NUMBER -> new NumberValue(token.getNumber());
            case PLACEHOLDER -> new NumberValue(token.getString());
            case ENUM -> new EnumValue(token.getString());
            case BLOCK_OPEN -> {
                if (!allowLists) {
                    throw new ParsingException("Cannot recurse list values");
                }
                ArrayValue values = new ArrayValue();
                while (lexer.hasNext() && !lexer.peek().isOf(TokenType.BLOCK_CLOSE)) {
                    values.add(parseValue(lexer, false));
                    if (!lexer.peek().isOf(TokenType.BLOCK_CLOSE)) {
                        lexer.expect(TokenType.COMMA);
                    }
                }
                lexer.expect(TokenType.BLOCK_CLOSE);
                yield values;
            }
            case SELECTOR_OPEN -> {
                String selector = null;
                if (!lexer.peek().isOf(TokenType.SELECTOR_CLOSE)) {
                    selector = lexer.expect(TokenType.LITERAL).getString();
                }
                lexer.expect(TokenType.SELECTOR_CLOSE);
                String name = lexer.expectIdentifier().getString();
                yield new GameValue(name, selector);
            }
            default -> throw new ParsingException("Invalid value");
        };
    }

    public void write(Writer writer) throws IOException {
        try (JsonWriter json = new JsonWriter(writer)) {
            json.beginObject();
            json.name("handlers");
            json.beginArray();
            for (int i = 0; i < handlers.size(); ++i) {
                writeHandler(json, handlers.get(i), isCompact ? 0 : i);
            }
            json.endArray();
            json.endObject();
        }
    }

    private static void writeHandler(JsonWriter writer, Handler handler, int position) throws IOException {
        writer.beginObject();
        writer.name("position");
        writer.value(position);
        writer.name("type");
        HandlerType type = handler.getType();
        writer.value(type.getName());
        writer.name(type.getNameField());
        writer.value(handler.getName());
        writer.name("operations");
        writer.beginArray();
        for (Operation operation : handler.getOperations()) {
            writeOperation(writer, operation);
        }
        writer.endArray();
        writer.endObject();
    }

    private static void writeOperation(JsonWriter writer, Operation operation) throws IOException {
        writer.beginObject();
        writer.name("action");
        writer.value(operation.getName());
        if (operation.hasDelegate()) {
            writer.name("conditional");
            writer.beginObject();
            writer.name("action");
            writer.value(operation.getDelegate());
            writer.name("is_inverted");
            writer.value(operation.isInverted());
            writer.endObject();
        } else if (operation.isInverted()) {
            writer.name("is_inverted");
            writer.value(true);
        }
        if (operation.hasSelector()) {
            writer.name("selection");
            writer.beginObject();
            writer.name("type");
            writer.value(operation.getSelector());
            writer.endObject();
        }
        writer.name("values");
        writer.beginArray();
        for (Map.Entry<String, Value> entry : operation.getArguments().entrySet()) {
            writer.beginObject();
            writer.name("name");
            writer.value(entry.getKey());
            writer.name("value");
            entry.getValue().writeJson(writer);
            writer.endObject();
        }
        writer.endArray();
        if (!operation.getOperations().isEmpty()) {
            writer.name("operations");
            writer.beginArray();
            for (Operation innerOperation : operation.getOperations()) {
                writeOperation(writer, innerOperation);
            }
            writer.endArray();
        }
        writer.endObject();
    }
}
