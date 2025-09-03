package me.white.justice.value;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.white.justice.Marshal;
import me.white.justice.MarshalException;
import me.white.justice.ParsingException;
import me.white.justice.lexer.Lexer;
import me.white.justice.lexer.TokenType;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.SNBTParser;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;

import java.io.IOException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public enum ValueType {
    NUMBER("number", false) {
        @Override
        public Value marshal(JsonObject object) {
            JsonPrimitive primitive = object.get("number").getAsJsonPrimitive();
            if (primitive.isString()) {
                return new NumberValue(primitive.getAsString());
            }
            return new NumberValue(primitive.getAsDouble());
        }
    },
    TEXT("text", false) {
        @Override
        public Value marshal(JsonObject object) throws MarshalException {
            String text = object.get("text").getAsString();
            TextParsing parsing = TextParsing.byName(object.get("parsing").getAsString());
            if (parsing == null) {
                throw new MarshalException("Invalid text parsing");
            }
            return new TextValue(text, parsing);
        }
    },
    ARRAY("array", false) {
        @Override
        public Value marshal(JsonObject object) throws MarshalException {
            ArrayValue array = new ArrayValue();
            JsonArray values = Marshal.jsonGet(object, "values").getAsJsonArray();
            for (JsonElement element : values) {
                Value innerValue = Marshal.marshalValue(element.getAsJsonObject(), false);
                if (innerValue != null) {
                    array.add(innerValue);
                }
            }
            return array;
        }
    },
    VARIABLE("variable", false) {
        @Override
        public Value marshal(JsonObject object) throws MarshalException {
            String name = Marshal.jsonGet(object, "variable").getAsString();
            VariableScope scope = VariableScope.byName(object.get("scope").getAsString());
            if (scope == null) {
                throw new MarshalException("Invalid variable scope");
            }
            return new VariableValue(name, scope);
        }
    },
    GAME("game_value", false) {
        @Override
        public Value marshal(JsonObject object) {
            String name = object.get("game_value").getAsString();
            String selector = object.get("selection").getAsString();
            if (selector.equals("null")) {
                return new GameValue(name, null);
            }
            selector = selector.substring(10, selector.length() - 2);
            return new GameValue(name, selector);
        }
    },
    LOCATION("location", true) {
        @Override
        public Value parse(Lexer lexer) throws ParsingException {
            lexer.expect(TokenType.BLOCK_OPEN);
            double x = (double)lexer.expect(TokenType.NUMBER).getValue();
            lexer.expect(TokenType.COMMA);
            double y = (double)lexer.expect(TokenType.NUMBER).getValue();
            lexer.expect(TokenType.COMMA);
            double z = (double)lexer.expect(TokenType.NUMBER).getValue();
            double yaw = 0;
            double pitch = 0;
            if (!lexer.peek().isOf(TokenType.BLOCK_CLOSE)) {
                lexer.expect(TokenType.COMMA);
                yaw = (double)lexer.expect(TokenType.NUMBER).getValue();
                lexer.expect(TokenType.COMMA);
                pitch = (double)lexer.expect(TokenType.NUMBER).getValue();
            }
            lexer.expect(TokenType.BLOCK_CLOSE);
            return new LocationValue(x, y, z, yaw, pitch);
        }

        @Override
        public Value marshal(JsonObject object) {
            double x = object.get("x").getAsDouble();
            double y = object.get("y").getAsDouble();
            double z = object.get("z").getAsDouble();
            double yaw = object.get("yaw").getAsDouble();
            double pitch = object.get("pitch").getAsDouble();
            return new LocationValue(x, y, z, yaw, pitch);
        }
    },
    VECTOR("vector", true) {
        @Override
        public Value parse(Lexer lexer) throws ParsingException {
            lexer.expect(TokenType.BLOCK_OPEN);
            double x = (double)lexer.expect(TokenType.NUMBER).getValue();
            lexer.expect(TokenType.COMMA);
            double y = (double)lexer.expect(TokenType.NUMBER).getValue();
            lexer.expect(TokenType.COMMA);
            double z = (double)lexer.expect(TokenType.NUMBER).getValue();
            lexer.expect(TokenType.BLOCK_CLOSE);
            return new VectorValue(x, y, z);
        }

        @Override
        public Value marshal(JsonObject object) {
            double x = object.get("x").getAsDouble();
            double y = object.get("y").getAsDouble();
            double z = object.get("z").getAsDouble();
            return new VectorValue(x, y, z);
        }
    },
    ITEM("item", true) {
        @Override
        public Value parse(Lexer lexer) throws ParsingException {
            Tag<?> tag;
            SNBTParser parser = new SNBTParser(lexer.getBuffer().substring(lexer.getReadPos()));
            try {
                tag = parser.parse(Tag.DEFAULT_MAX_DEPTH, true);
            } catch (net.querz.nbt.io.ParseException e) {
                throw new ParsingException("Invalid item data: " + e.getMessage());
            }
            if (!(tag instanceof CompoundTag)) {
                throw new ParsingException("Invalid item data");
            }
            lexer.advance(parser.getReadChars() - 1);
            return new ItemValue((CompoundTag)tag);
        }

        @Override
        public Value marshal(JsonObject object) throws MarshalException {
            String serialized = object.get("item").getAsString();
            byte[] bytes = Base64.getDecoder().decode(serialized);
            Tag<?> item;
            try {
                item = new NBTDeserializer().fromBytes(bytes).getTag();
            } catch (IOException e) {
                throw new MarshalException("Incomplete item value");
            }
            if (!(item instanceof CompoundTag)) {
                throw new MarshalException("Malformed item value");
            }
            return new ItemValue((CompoundTag)item);
        }
    },
    SOUND("sound", true) {
        @Override
        public Value parse(Lexer lexer) throws ParsingException {
            lexer.expect(TokenType.BLOCK_OPEN);
            String name = (String)lexer.expect(TokenType.STRING).getValue();
            double volume = 1;
            double pitch = 1;
            String source = null;
            String variant = null;
            Set<String> seenComponents = new HashSet<>();
            while (lexer.hasNext() && !lexer.peek().isOf(TokenType.BLOCK_CLOSE)) {
                lexer.expect(TokenType.COMMA);
                String component = (String)lexer.expect(TokenType.LITERAL).getValue();
                if (seenComponents.contains(component)) {
                    throw new ParsingException("Duplicate sound component '" + component + "'");
                }
                seenComponents.add(component);
                lexer.expect(TokenType.EQUALS);
                switch (component) {
                    case "volume" -> volume = (double)lexer.expect(TokenType.NUMBER).getValue();
                    case "pitch" -> pitch = (double)lexer.expect(TokenType.NUMBER).getValue();
                    case "source" -> source = (String)lexer.expect(TokenType.STRING).getValue();
                    case "variant" -> variant = (String)lexer.expect(TokenType.STRING).getValue();
                    default -> throw new ParsingException("Invalid sound component '" + component + "'");
                }
            }
            lexer.expect(TokenType.BLOCK_CLOSE);
            return new SoundValue(name, volume, pitch, source, variant);
        }

        @Override
        public Value marshal(JsonObject object) {
            String name = object.get("sound").getAsString();
            double pitch = object.get("pitch").getAsDouble();
            double volume = object.get("volume").getAsDouble();
            String variant = null;
            String source = null;
            if (object.has("variation")) {
                variant = object.get("variation").getAsString();
                if (variant.isEmpty()) {
                    variant = null;
                }
            }
            if (object.has("source")) {
                source = object.get("source").getAsString();
                if (source.equals("MASTER")) {
                    source = null;
                }
            }
            return new SoundValue(name, pitch, volume, variant, source);
        }
    },
    POTION("potion", true) {
        @Override
        public Value parse(Lexer lexer) throws ParsingException {
            lexer.expect(TokenType.BLOCK_OPEN);
            String name = (String)lexer.expect(TokenType.STRING).getValue();
            int amplifier = 0;
            int duration = -1;
            Set<String> seenComponents = new HashSet<>();
            while (lexer.hasNext() && !lexer.peek().isOf(TokenType.BLOCK_CLOSE)) {
                lexer.expect(TokenType.COMMA);
                String component = (String)lexer.expect(TokenType.LITERAL).getValue();
                if (seenComponents.contains(component)) {
                    throw new ParsingException("Duplicate potion component '" + component + "'");
                }
                seenComponents.add(component);
                lexer.expect(TokenType.EQUALS);
                switch (component) {
                    case "amplifier" -> {
                        amplifier = (int)lexer.expect(TokenType.NUMBER).getValue();
                        if (amplifier < 0) {
                            throw new ParsingException("Negative potion amplifier");
                        }
                    }
                    case "duration" -> duration = (int)lexer.expect(TokenType.NUMBER).getValue();
                    default -> throw new ParsingException("Invalid potion component '" + component + "'");
                }
            }
            lexer.expect(TokenType.BLOCK_CLOSE);
            return new PotionValue(name, amplifier, duration);
        }

        @Override
        public Value marshal(JsonObject object) {
            String name = object.get("potion").getAsString();
            int amplifier = object.get("amplifier").getAsInt();
            int duration = object.get("duration").getAsInt();
            return new PotionValue(name, amplifier, duration);
        }
    },
    PARTICLE("particle", true) {
        @Override
        public Value parse(Lexer lexer) throws ParsingException {
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
            while (lexer.hasNext() && !lexer.peek().isOf(TokenType.BLOCK_CLOSE)) {
                lexer.expect(TokenType.COMMA);
                String component = (String)lexer.expect(TokenType.LITERAL).getValue();
                if (seenComponents.contains(component)) {
                    throw new ParsingException("Duplicate particle component '" + component + "'");
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
                    default -> throw new ParsingException("Invalid particle component '" + component + "'");
                }
            }
            lexer.expect(TokenType.BLOCK_CLOSE);
            return new ParticleValue(name, material, spreadH, spreadV, motionX, motionY, motionZ, count, color, size);
        }

        @Override
        public Value marshal(JsonObject object) {
            String name = object.get("particle_type").getAsString();
            String material = null;
            double spreadH = 0;
            double spreadV = 0;
            double motionX = 0;
            double motionY = 0;
            double motionZ = 0;
            int count = 1;
            int color = 0xFF0000;
            double size = 1;
            if (object.has("material")) {
                material = object.get("material").getAsString();
            }
            if (object.has("first_spread")) {
                spreadH = object.get("first_spread").getAsDouble();
                spreadV = object.get("second_spread").getAsDouble();
            }
            if (object.has("x_motion")) {
                motionX = object.get("x_motion").getAsDouble();
                motionY = object.get("y_motion").getAsDouble();
                motionZ = object.get("z_motion").getAsDouble();
            }
            if (object.has("count")) {
                count = object.get("count").getAsInt();
            }
            if (object.has("color")) {
                color = object.get("color").getAsInt();
            }
            if (object.has("size")) {
                size = object.get("size").getAsDouble();
            }
            return new ParticleValue(name, material, spreadH, spreadV, motionX, motionY, motionZ, count, color, size);
        }
    },
    ENUM("enum", false) {
        @Override
        public Value marshal(JsonObject object) {
            return new EnumValue(object.get("enum").getAsString());
        }
    };

    final String name;
    final boolean isFactory;

    ValueType(String name, boolean isFactory) {
        this.name = name;
        this.isFactory = isFactory;
    }

    public static ValueType byName(String name) {
        for (ValueType type : ValueType.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public boolean isFactory() {
        return isFactory;
    }

    public Value parse(Lexer lexer) throws ParsingException {
        throw new UnsupportedOperationException();
    }

    public Value marshal(JsonObject object) throws MarshalException {
        throw new UnsupportedOperationException();
    }
}
