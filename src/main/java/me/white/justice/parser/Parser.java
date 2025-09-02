package me.white.justice.parser;

import me.white.justice.CompilationException;
import me.white.justice.lexer.Lexer;
import me.white.justice.lexer.Token;
import me.white.justice.lexer.TokenType;
import me.white.justice.value.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private Value parseValue(boolean allowLists) throws CompilationException {
        Token token = lexer.read();
        return switch (token.getType()) {
            case LITERAL -> {
                String name = (String)token.getValue();
                if (name.length() == 1) {
                    char prefix = name.charAt(0);
                    Token next = lexer.peek();
                    if (next.isOf(TokenType.STRING)) {
                        lexer.read();
                        TextParsing parsing = TextParsing.byPrefix(prefix);
                        if (parsing == null) {
                            throw new CompilationException("Invalid text parsing '" + prefix + "'");
                        }
                        yield new TextValue(parsing, (String)next.getValue());
                    }
                    if (next.isOf(TokenType.IDENTIFIER)) {
                        lexer.read();
                        VariableScope scope = VariableScope.byPrefix(prefix);
                        if (scope == null) {
                            throw new CompilationException("Invalid variable scope '" + prefix + "'");
                        }
                        yield new VariableValue(scope, (String)next.getValue());
                    }
                }
                ValueType type = ValueType.byName(name);
                if (type != null && type.isFactory() && lexer.peek().isOf(TokenType.BLOCK_OPEN)) {
                    yield type.read(lexer);
                }
                yield new VariableValue(VariableScope.LOCAL, name);
            }
            case IDENTIFIER -> new VariableValue(VariableScope.LOCAL, (String)token.getValue());
            case STRING -> new TextValue(TextParsing.PLAIN, (String)token.getValue());
            case NUMBER -> new NumberValue((double)token.getValue());
            case PLACEHOLDER -> new NumberValue((String)token.getValue());
            case ENUM -> new EnumValue((String)token.getValue());
            case BLOCK_OPEN -> {
                if (!allowLists) {
                    throw new CompilationException("Cannot recurse list values");
                }
                ArrayValue values = new ArrayValue();
                while (lexer.hasNext() && !lexer.peek().isOf(TokenType.BLOCK_CLOSE)) {
                    values.add(parseValue(false));
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
                    selector = (String)lexer.expect(TokenType.LITERAL).getValue();
                }
                lexer.expect(TokenType.SELECTOR_CLOSE);
                String name = (String)lexer.expectIdentifier().getValue();
                yield new GameValue(selector, name);
            }
            default -> throw new CompilationException("Invalid value");
        };
    }

    private Operation parseOperation() throws CompilationException {
        boolean isInverted = false;
        Token name = lexer.expectIdentifier();
        Token delegate = null;
        String selector = null;
        Map<String, Value> arguments = new HashMap<>();
        List<Operation> operations = new ArrayList<>();
        if (lexer.peek().getType().isIdentifier()) {
            delegate = lexer.expectIdentifier();
        }
        if (name.isOf(TokenType.LITERAL) && name.getValue().equals("not")) {
            isInverted = true;
            if (delegate == null) {
                throw new CompilationException("Inversion without an action");
            }
            name = delegate;
            delegate = null;
        } else if (delegate != null && delegate.isOf(TokenType.LITERAL) && delegate.getValue().equals("not")) {
            isInverted = true;
            delegate = lexer.expectIdentifier();
        }
        if (lexer.peek().isOf(TokenType.SELECTOR_OPEN)) {
            lexer.read();
            selector = (String)lexer.expect(TokenType.LITERAL).getValue();
            lexer.expect(TokenType.SELECTOR_CLOSE);
        }
        if (lexer.peek().isOf(TokenType.ARGS_OPEN)) {
            lexer.read();
            while (lexer.hasNext() && !lexer.peek().isOf(TokenType.ARGS_CLOSE)) {
                String argument = (String)lexer.expect(TokenType.LITERAL).getValue();
                lexer.expect(TokenType.EQUALS);
                Value value = parseValue(true);
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
                operations.add(parseOperation());
            }
            lexer.expect(TokenType.BLOCK_CLOSE);
        } else {
            lexer.expect(TokenType.EOL);
        }
        return new Operation(isInverted, (String)name.getValue(), selector, arguments, operations, delegate == null ? null : (String)delegate.getValue());
    }

    private Handler parseHandler() throws CompilationException {
        List<Operation> operations = new ArrayList<>();
        HandlerType type = HandlerType.FUNCTION;
        Token name = lexer.expectIdentifier();
        if (!name.isOf(TokenType.IDENTIFIER)) {
            HandlerType constitute = HandlerType.byName((String)name.getValue());
            if (constitute != null) {
                type = constitute;
                name = lexer.expectIdentifier();
            }
        }
        lexer.expect(TokenType.BLOCK_OPEN);
        while (lexer.hasNext() && !lexer.peek().isOf(TokenType.BLOCK_CLOSE)) {
            operations.add(parseOperation());
        }
        lexer.expect(TokenType.BLOCK_CLOSE);
        return new Handler(type, (String)name.getValue(), operations);
    }

    public List<Handler> parse() throws CompilationException {
        List<Handler> handlers = new ArrayList<>();
        while (lexer.hasNext()) {
            handlers.add(parseHandler());
        }
        return handlers;
    }
}
