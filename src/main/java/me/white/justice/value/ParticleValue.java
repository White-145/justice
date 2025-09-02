package me.white.justice.value;

import com.google.gson.stream.JsonWriter;
import me.white.justice.CompilationException;
import me.white.justice.lexer.Lexer;
import me.white.justice.lexer.TokenType;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class ParticleValue implements Value {
    private final String name;
    private final String material;
    private final double spreadH;
    private final double spreadV;
    private final double motionX;
    private final double motionY;
    private final double motionZ;
    private final int count;
    private final int color;
    private final double size;

    public ParticleValue(String name, String material, double spreadH, double spreadV, double motionX, double motionY, double motionZ, int count, int color, double size) {
        this.name = name;
        this.material = material;
        this.spreadH = spreadH;
        this.spreadV = spreadV;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.count = count;
        this.color = color;
        this.size = size;
    }

    public static Value parse(Lexer lexer) throws CompilationException {
        lexer.expect(TokenType.BLOCK_OPEN);
        String name = (String)lexer.expect(TokenType.STRING).getValue();
        String material = null;
        double spreadH = 0;
        double spreadV = 0;
        double motionX = 0;
        double motionY = 0;
        double motionZ = 0;
        int count = 1;
        int color = 0xFF0000;
        double size = 1;
        Set<String> seenComponents = new HashSet<>();
        while (lexer.canLex() && !lexer.guess(TokenType.BLOCK_CLOSE, false)) {
            lexer.expect(TokenType.COMMA);
            String component = (String)lexer.expect(TokenType.LITERAL).getValue();
            if (seenComponents.contains(component)) {
                throw new CompilationException("Duplicate particle component '" + component + "'");
            }
            seenComponents.add(component);
            lexer.expect(TokenType.EQUALS);
            switch (component) {
                case "material" -> material = (String)lexer.expect(TokenType.STRING).getValue();
                case "spread" -> {
                    lexer.expect(TokenType.BLOCK_OPEN);
                    spreadH = (double)lexer.expect(TokenType.NUMBER).getValue();
                    lexer.expect(TokenType.COMMA);
                    spreadV = (double)lexer.expect(TokenType.NUMBER).getValue();
                    lexer.expect(TokenType.BLOCK_CLOSE);
                }
                case "motion" -> {
                    lexer.expect(TokenType.BLOCK_OPEN);
                    motionX = (double)lexer.expect(TokenType.NUMBER).getValue();
                    lexer.expect(TokenType.COMMA);
                    motionY = (double)lexer.expect(TokenType.NUMBER).getValue();
                    lexer.expect(TokenType.COMMA);
                    motionZ = (double)lexer.expect(TokenType.NUMBER).getValue();
                    lexer.expect(TokenType.BLOCK_CLOSE);
                }
                case "count" -> count = (int)lexer.expect(TokenType.NUMBER).getValue();
                case "color" -> color = (int)lexer.expect(TokenType.COLOR).getValue();
                case "size" -> size = (double)lexer.expect(TokenType.NUMBER).getValue();
                default -> throw new CompilationException("Invalid particle component '" + component + "'");
            }
        }
        lexer.expect(TokenType.BLOCK_CLOSE);
        return new ParticleValue(name, material, spreadH, spreadV, motionX, motionY, motionZ, count, color, size);
    }

    public String getName() {
        return name;
    }

    public boolean hasMaterial() {
        return material != null;
    }

    public String getMaterial() {
        return material;
    }

    public boolean hasSpread() {
        return spreadH != 0 || spreadV != 0;
    }

    public double getSpreadH() {
        return spreadH;
    }

    public double getSpreadV() {
        return spreadV;
    }

    public boolean hasMotion() {
        return motionX != 0 || motionY != 0 || motionZ != 0;
    }

    public double getMotionX() {
        return motionX;
    }

    public double getMotionY() {
        return motionY;
    }

    public double getMotionZ() {
        return motionZ;
    }

    public int getCount() {
        return count;
    }

    public int getColor() {
        return color;
    }

    public double getSize() {
        return size;
    }

    @Override
    public ValueType getType() {
        return ValueType.PARTICLE;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.write("particle{ ");
        Value.writeString(writer, name);
        if (hasMaterial()) {
            writer.write(", material=\"");
            writer.write(material);
            writer.write("\"");
        }
        if (hasSpread()) {
            writer.write(", spread={ ");
            Value.writeNumber(writer, spreadH);
            writer.write(", ");
            Value.writeNumber(writer, spreadV);
            writer.write(" }");
        }
        if (hasMotion()) {
            writer.write(", motion={ ");
            Value.writeNumber(writer, motionX);
            writer.write(", ");
            Value.writeNumber(writer, motionY);
            writer.write(", ");
            Value.writeNumber(writer, motionZ);
            writer.write(" }");
        }
        if (count != 1) {
            writer.write(", count=");
            writer.write(Integer.toString(count));
        }
        if (color != 0xFF0000) {
            writer.write(String.format(", color=#%06X", color));
        }
        if (size != 1) {
            writer.write(", size=");
            Value.writeNumber(writer, size);
        }
        writer.write(" }");
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type");
        writer.value(getType().getName());
        writer.name("particle_type");
        writer.value(name);
        if (hasMaterial()) {
            writer.name("material");
            writer.value(material);
        }
        writer.name("count");
        writer.value(count);
        writer.name("first_spread");
        writer.value(spreadH);
        writer.name("second_spread");
        writer.value(spreadV);
        writer.name("x_motion");
        writer.value(motionX);
        writer.name("y_motion");
        writer.value(motionY);
        writer.name("z_motion");
        writer.value(motionZ);
        writer.name("color");
        writer.value(color);
        writer.name("size");
        writer.value(size);
        writer.endObject();
    }
}
