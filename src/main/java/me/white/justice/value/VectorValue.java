package me.white.justice.value;

import com.google.gson.stream.JsonWriter;
import me.white.justice.CompilationException;
import me.white.justice.lexer.Lexer;
import me.white.justice.lexer.TokenType;

import java.io.IOException;
import java.io.Writer;

public class VectorValue implements Value {
    private final double x;
    private final double y;
    private final double z;

    public VectorValue(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Value parse(Lexer lexer) throws CompilationException {
        lexer.expect(TokenType.BLOCK_OPEN);
        double x = (double)lexer.expect(TokenType.NUMBER).getValue();
        lexer.expect(TokenType.COMMA);
        double y = (double)lexer.expect(TokenType.NUMBER).getValue();
        lexer.expect(TokenType.COMMA);
        double z = (double)lexer.expect(TokenType.NUMBER).getValue();
        lexer.expect(TokenType.BLOCK_CLOSE);
        return new VectorValue(x, y, z);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public ValueType getType() {
        return ValueType.VECTOR;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.write("vector{ ");
        Value.writeNumber(writer, x);
        writer.write(", ");
        Value.writeNumber(writer, y);
        writer.write(", ");
        Value.writeNumber(writer, z);
        writer.write(" }");
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(getType().getName());
        writer.name("x");
        writer.value(x);
        writer.name("y");
        writer.value(y);
        writer.name("z");
        writer.value(z);
        writer.endObject();
    }
}
