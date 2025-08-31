package me.white.justice;

import me.white.justice.lexer.Lexer;
import me.white.justice.parser.Handler;
import me.white.justice.parser.Parser;

import java.util.List;

public class Compiler {
    public static List<Handler> compile(String source) throws CompilationException {
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer);
        return parser.parse();
    }
}
