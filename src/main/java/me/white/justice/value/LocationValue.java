package me.white.justice.value;

import com.google.gson.stream.JsonWriter;
import me.white.justice.CompilationException;
import me.white.justice.lexer.Lexer;
import me.white.justice.lexer.TokenType;

import java.io.IOException;

public class LocationValue implements Value {
    private final double x;
    private final double y;
    private final double z;
    private final double yaw;
    private final double pitch;

    public LocationValue(double x, double y, double z, double yaw, double pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static Value parse(Lexer lexer) throws CompilationException {
        lexer.expect(TokenType.BLOCK_OPEN);
        double x = (double)lexer.expect(TokenType.NUMBER).getValue();
        lexer.expect(TokenType.COMMA);
        double y = (double)lexer.expect(TokenType.NUMBER).getValue();
        lexer.expect(TokenType.COMMA);
        double z = (double)lexer.expect(TokenType.NUMBER).getValue();
        double yaw = 0;
        double pitch = 0;
        if (!lexer.guess(TokenType.BLOCK_CLOSE, false)) {
            lexer.expect(TokenType.COMMA);
            yaw = (double)lexer.expect(TokenType.NUMBER).getValue();
            lexer.expect(TokenType.COMMA);
            pitch = (double)lexer.expect(TokenType.NUMBER).getValue();
        }
        lexer.expect(TokenType.BLOCK_CLOSE);
        return new LocationValue(x, y, z, yaw, pitch);
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

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

    @Override
    public ValueType getType() {
        return ValueType.LOCATION;
    }

    @Override
    public void write(JsonWriter writer) throws IOException {
        writer.name("x");
        writer.value(x);
        writer.name("y");
        writer.value(y);
        writer.name("z");
        writer.value(z);
        writer.name("yaw");
        writer.value(yaw);
        writer.name("pitch");
        writer.value(pitch);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("location{ ");
        builder.append(Math.round(x * 100) / 100);
        builder.append(", ");
        builder.append(Math.round(y * 100) / 100);
        builder.append(", ");
        builder.append(Math.round(z * 100) / 100);
        if (yaw != 0 || pitch != 0) {
            builder.append(", ");
            builder.append(Math.round(yaw * 100) / 100);
            builder.append(", ");
            builder.append(Math.round(pitch * 100) / 100);
        }
        builder.append(" }");
        return builder.toString();
    }
}
