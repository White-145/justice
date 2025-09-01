package me.white.justice;

import com.google.gson.stream.JsonWriter;
import me.white.justice.lexer.Lexer;
import me.white.justice.parser.Handler;
import me.white.justice.parser.HandlerType;
import me.white.justice.parser.Operation;
import me.white.justice.parser.Parser;
import me.white.justice.value.*;
import net.querz.nbt.io.NBTSerializer;
import net.querz.nbt.io.NamedTag;

import java.io.IOException;
import java.io.Writer;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class Compiler {
    public static List<Handler> parse(String source) throws CompilationException {
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer);
        return parser.parse();
    }

    public static void write(Writer writer, List<Handler> handlers) throws IOException {
        try (JsonWriter json = new JsonWriter(writer)) {
            json.beginObject();
            json.name("handlers");
            json.beginArray();
            for (int i = 0; i < handlers.size(); ++i) {
                Handler handler = handlers.get(i);
                json.beginObject();
                json.name("position");
                json.value(i);
                writeHandler(json, handler);
                json.endObject();
            }
            json.endArray();
            json.endObject();
        }
    }

    private static void writeHandler(JsonWriter json, Handler handler) throws IOException {
        json.name("type");
        HandlerType type = handler.getType();
        json.value(type.getName());
        if (type == HandlerType.EVENT) {
            json.name("event");
        } else {
            json.name("name");
        }
        json.value(handler.getName());
        json.name("operations");
        json.beginArray();
        for (Operation operation : handler.getOperations()) {
            json.beginObject();
            writeOperation(json, operation);
            json.endObject();
        }
        json.endArray();
    }

    private static void writeOperation(JsonWriter json, Operation operation) throws IOException {
        json.name("action");
        json.value(operation.getName());
        if (operation.hasDelegate()) {
            json.name("conditional");
            json.beginObject();
            json.name("action");
            json.value(operation.getDelegate());
            json.name("is_inverted");
            json.value(operation.isInverted());
            json.endObject();
        } else if (operation.isInverted()) {
            json.name("is_inverted");
            json.value(true);
        }
        if (operation.hasSelector()) {
            json.name("selection");
            json.beginObject();
            json.name("type");
            json.value(operation.getSelector());
            json.endObject();
        }
        json.name("values");
        json.beginArray();
        if (!operation.getArguments().isEmpty()) {
            for (Map.Entry<String, Value> entry : operation.getArguments().entrySet()) {
                json.beginObject();
                json.name("name");
                json.value(entry.getKey());
                json.name("value");
                json.beginObject();
                writeValue(json, entry.getValue(), true);
                json.endObject();
                json.endObject();
            }
        }
        json.endArray();
        if (!operation.getOperations().isEmpty()) {
            json.name("operations");
            json.beginArray();
            for (Operation innerOperation : operation.getOperations()) {
                json.beginObject();
                writeOperation(json, innerOperation);
                json.endObject();
            }
            json.endArray();
        }
    }

    private static void writeValue(JsonWriter json, Value value, boolean allowArrays) throws IOException {
        json.name("type");
        json.value(value.getType().getName());
        switch (value.getType()) {
            case NUMBER -> {
                NumberValue number = (NumberValue)value;
                json.name("number");
                if (number.isPlaceholder()) {
                    json.value(number.getPlaceholder());
                } else {
                    json.value(number.getNumber());
                }
            }
            case TEXT -> {
                TextValue text = (TextValue)value;
                json.name("text");
                json.value(text.getText());
                json.name("parsing");
                json.value(text.getTextParsing().getName());
            }
            case ARRAY -> {
                if (!allowArrays) {
                    throw null; // no arrays
                }
                ArrayValue array = (ArrayValue)value;
                json.name("values");
                json.beginArray();
                for (Value innerValue : array) {
                    json.beginObject();
                    writeValue(json, innerValue, false);
                    json.endObject();
                }
                json.endArray();
            }
            case VARIABLE -> {
                VariableValue variable = (VariableValue)value;
                json.name("variable");
                json.value(variable.getName());
                json.name("scope");
                json.value(variable.getScope().getName());
            }
            case GAME -> {
                GameValue game = (GameValue)value;
                json.name("game_value");
                json.value(game.getName());
                json.name("selection");
                if (game.hasSelector()) {
                    json.value("{\"type\":\"" + game.getSelector() + "\"}");
                } else {
                    json.value("null");
                }
            }
            case LOCATION -> {
                LocationValue location = (LocationValue)value;
                json.name("x");
                json.value(location.getX());
                json.name("y");
                json.value(location.getY());
                json.name("z");
                json.value(location.getZ());
                json.name("yaw");
                json.value(location.getYaw());
                json.name("pitch");
                json.value(location.getPitch());
            }
            case VECTOR -> {
                VectorValue vector = (VectorValue)value;
                json.name("x");
                json.value(vector.getX());
                json.name("y");
                json.value(vector.getY());
                json.name("z");
                json.value(vector.getZ());
            }
            case ITEM -> {
                ItemValue item = (ItemValue)value;
                byte[] bytes = new NBTSerializer(true).toBytes(new NamedTag(null, item.getItem()));
                String serialized = Base64.getEncoder().encodeToString(bytes);
                json.name("item");
                json.value(serialized);
            }
            case SOUND -> {
                SoundValue sound = (SoundValue)value;
                json.name("sound");
                json.value(sound.getName());
                json.name("pitch");
                json.value(sound.getPitch());
                json.name("volume");
                json.value(sound.getVolume());
                if (sound.hasVariant()) {
                    json.name("variation");
                    json.value(sound.getVariant());
                }
                if (sound.hasSource()) {
                    json.name("source");
                    json.value(sound.getSource());
                }
            }
            case POTION -> {
                PotionValue potion = (PotionValue)value;
                json.name("potion");
                json.value(potion.getName());
                json.name("amplifier");
                json.value(potion.getAmplifier());
                json.name("duration");
                json.value(potion.getDuration());
            }
            case PARTICLE -> {
                ParticleValue particle = (ParticleValue)value;
                json.name("particle_type");
                json.value(particle.getName());
                if (particle.hasMaterial()) {
                    json.name("material");
                    json.value(particle.getMaterial());
                }
                json.name("count");
                json.value(particle.getCount());
                json.name("first_spread");
                json.value(particle.getSpreadH());
                json.name("second_spread");
                json.value(particle.getSpreadV());
                json.name("x_motion");
                json.value(particle.getMotionX());
                json.name("y_motion");
                json.value(particle.getMotionY());
                json.name("z_motion");
                json.value(particle.getMotionZ());
                json.name("color");
                json.value(particle.getColor());
                json.name("size");
                json.value(particle.getSize());
            }
            case ENUM -> {
                EnumValue enumValue = (EnumValue)value;
                json.name("enum");
                json.value(enumValue.getName());
            }
        }
    }
}
