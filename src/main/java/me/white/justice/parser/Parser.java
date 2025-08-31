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
                        lexer.skip(1);
                        TextParsing parsing = TextParsing.byPrefix(prefix);
                        if (parsing == null) {
                            throw new CompilationException("Invalid text parsing '" + prefix + "'");
                        }
                        yield new TextValue(parsing, (String)next.getValue());
                    }
                    if (next.isOf(TokenType.IDENTIFIER)) {
                        lexer.skip(1);
                        VariableScope scope = VariableScope.byPrefix(prefix);
                        if (scope == null) {
                            throw new CompilationException("Invalid variable scope '" + prefix + "'");
                        }
                        yield new VariableValue(scope, (String)next.getValue());
                    }
                }
                FactoryValue factory = FactoryValue.byName(name);
                if (factory != null && lexer.guess(TokenType.BLOCK_OPEN, false)) {
                    yield factory.parse(lexer);
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
                while (lexer.canLex() && !lexer.guess(TokenType.BLOCK_CLOSE, false)) {
                    values.add(parseValue(false));
                    if (!lexer.guess(TokenType.BLOCK_CLOSE, false)) {
                        lexer.expect(TokenType.COMMA);
                    }
                }
                lexer.expect(TokenType.BLOCK_CLOSE);
                yield values;
            }
            default -> throw new CompilationException("Invalid value");
        };
    }

    private Operation parseInstruction() throws CompilationException {
        boolean isInverted = false;
        Token name = lexer.expectIdentifier();
        Token delegate = null;
        String selector = null;
        Map<String, Value> arguments = null;
        List<Operation> operations = null;
        if (lexer.guessIdentifier(false)) {
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
        if (lexer.guess(TokenType.SELECTOR_OPEN, true)) {
            selector = (String)lexer.expect(TokenType.LITERAL).getValue();
            lexer.expect(TokenType.SELECTOR_CLOSE);
        }
        if (lexer.guess(TokenType.ARGS_OPEN, true)) {
            arguments = new HashMap<>();
            while (lexer.canLex() && !lexer.guess(TokenType.ARGS_CLOSE, false)) {
                String argument = (String)lexer.expect(TokenType.LITERAL).getValue();
                lexer.expect(TokenType.EQUALS);
                Value value = parseValue(true);
                arguments.put(argument, value);
                if (!lexer.guess(TokenType.ARGS_CLOSE, false)) {
                    lexer.expect(TokenType.COMMA);
                }
            }
            lexer.expect(TokenType.ARGS_CLOSE);
        }
        if (lexer.guess(TokenType.BLOCK_OPEN, true)) {
            operations = new ArrayList<>();
            while (lexer.canLex() && !lexer.guess(TokenType.BLOCK_CLOSE, false)) {
                operations.add(parseInstruction());
            }
            lexer.expect(TokenType.BLOCK_CLOSE);
        } else {
            lexer.expect(TokenType.EOL);
        }
        return new Operation(isInverted, (String)name.getValue(), selector, arguments, operations, delegate == null ? null : (String)delegate.getValue());
    }

    private Handler parseFunction() throws CompilationException {
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
        while (lexer.canLex() && !lexer.guess(TokenType.BLOCK_CLOSE, false)) {
            operations.add(parseInstruction());
        }
        lexer.expect(TokenType.BLOCK_CLOSE);
        return new Handler(type, (String)name.getValue(), operations);
    }

    public List<Handler> parse() throws CompilationException {
        List<Handler> handlers = new ArrayList<>();
        while (lexer.canLex()) {
            handlers.add(parseFunction());
        }
        return handlers;
    }
}
