package me.white.justice;

import com.google.gson.*;
import me.white.justice.lexer.Lexer;
import me.white.justice.parser.Handler;
import me.white.justice.parser.HandlerType;
import me.white.justice.parser.Operation;
import me.white.justice.value.*;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Decompiler {
    public static List<Handler> read(String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        List<Handler> functions = new ArrayList<>();
        JsonArray handlers = object.get("handlers").getAsJsonArray();
        for (JsonElement element : handlers) {
            JsonObject handler = element.getAsJsonObject();
            functions.add(decompileHandler(handler));
        }
        return functions;
    }

    private static Handler decompileHandler(JsonObject handler) {
        HandlerType type = HandlerType.byName(handler.get("type").getAsString());
        if (type == null) {
            throw null; // invalid handler type
        }
        String name;
        if (type == HandlerType.EVENT) {
            name = handler.get("event").getAsString();
        } else {
            name = handler.get("name").getAsString();
        }
        List<Operation> operations = new ArrayList<>();
        JsonArray operationsArray = handler.get("operations").getAsJsonArray();
        for (JsonElement element : operationsArray) {
            JsonObject operation = element.getAsJsonObject();
            operations.add(decompileOperation(operation));
        }
        return new Handler(type, name, operations);
    }

    private static Operation decompileOperation(JsonObject operation) {
        String name = operation.get("action").getAsString();
        String delegate = null;
        boolean isInverted = false;
        String selector = null;
        Map<String, Value> arguments = new HashMap<>();
        List<Operation> operations = new ArrayList<>();
        if (operation.has("conditional")) {
            JsonObject conditional = operation.get("conditional").getAsJsonObject();
            delegate = conditional.get("action").getAsString();
            if (conditional.has("is_inverted")) {
                isInverted = conditional.get("is_inverted").getAsBoolean();
            }
        } else if (operation.has("is_inverted")) {
            isInverted = operation.get("is_inverted").getAsBoolean();
        }
        if (operation.has("selection")) {
            JsonObject selection = operation.get("selection").getAsJsonObject();
            selector = selection.get("type").getAsString();
        }
        JsonArray values = operation.get("values").getAsJsonArray();
        for (JsonElement element : values) {
            JsonObject argument = element.getAsJsonObject();
            String valueName = argument.get("name").getAsString();
            JsonObject valueObject = argument.get("value").getAsJsonObject();
            Value value = decompileValue(valueObject, true);
            if (value != null) {
                arguments.put(valueName, value);
            }
        }
        if (operation.has("operations")) {
            JsonArray operationsArray = operation.get("operations").getAsJsonArray();
            for (JsonElement element : operationsArray) {
                JsonObject operationObject = element.getAsJsonObject();
                operations.add(decompileOperation(operationObject));
            }
        }
        return new Operation(isInverted, name, selector, arguments, operations, delegate);
    }

    private static Value decompileValue(JsonObject valueObject, boolean allowArrays) {
        if (valueObject.isEmpty()) {
            return null;
        }
        ValueType type = ValueType.byName(valueObject.get("type").getAsString());
        if (type == null) {
            throw null; // invalid type
        }
        return switch (type) {
            case NUMBER -> {
                JsonPrimitive primitive = valueObject.get("number").getAsJsonPrimitive();
                if (primitive.isString()) {
                    yield new NumberValue(primitive.getAsString());
                }
                yield new NumberValue(primitive.getAsDouble());
            }
            case TEXT -> {
                String text = valueObject.get("text").getAsString();
                TextParsing parsing = TextParsing.byName(valueObject.get("parsing").getAsString());
                if (parsing == null) {
                    throw null; // invalid text parsing
                }
                yield new TextValue(parsing, text);
            }
            case ARRAY -> {
                if (!allowArrays) {
                    throw null; // no recursing arrays
                }
                ArrayValue array = new ArrayValue();
                JsonArray values = valueObject.get("values").getAsJsonArray();
                for (JsonElement element : values) {
                    JsonObject object = element.getAsJsonObject();
                    Value value = decompileValue(object, false);
                    if (value != null) {
                        array.add(value);
                    }
                }
                yield array;
            }
            case VARIABLE -> {
                String name = valueObject.get("variable").getAsString();
                VariableScope scope = VariableScope.byName(valueObject.get("scope").getAsString());
                if (scope == null) {
                    throw null; // invalid variable scope
                }
                yield new VariableValue(scope, name);
            }
            case GAME -> {
                String name = valueObject.get("game_value").getAsString();
                String selector = valueObject.get("selection").getAsString();
                if (selector.equals("null")) {
                    yield new GameValue(null, name);
                }
                selector = selector.substring(10, selector.length() - 2);
                yield new GameValue(selector, name);
            }
            case LOCATION -> {
                double x = valueObject.get("x").getAsDouble();
                double y = valueObject.get("y").getAsDouble();
                double z = valueObject.get("z").getAsDouble();
                double yaw = valueObject.get("yaw").getAsDouble();
                double pitch = valueObject.get("pitch").getAsDouble();
                yield new LocationValue(x, y, z, yaw, pitch);
            }
            case VECTOR -> {
                double x = valueObject.get("x").getAsDouble();
                double y = valueObject.get("y").getAsDouble();
                double z = valueObject.get("z").getAsDouble();
                yield new VectorValue(x, y, z);
            }
            case ITEM -> {
                String serialized = valueObject.get("item").getAsString();
                byte[] bytes = Base64.getDecoder().decode(serialized);
                Tag<?> item;
                try {
                    item = new NBTDeserializer().fromBytes(bytes).getTag();
                } catch (IOException e) {
                    throw null; // malformed item
                }
                if (!(item instanceof CompoundTag)) {
                    throw null; // malformed item
                }
                yield new ItemValue((CompoundTag)item);
            }
            case SOUND -> {
                String name = valueObject.get("sound").getAsString();
                double pitch = valueObject.get("pitch").getAsDouble();
                double volume = valueObject.get("volume").getAsDouble();
                String variant = null;
                String source = null;
                if (valueObject.has("variation")) {
                    variant = valueObject.get("variation").getAsString();
                }
                if (valueObject.has("source")) {
                    valueObject.get("source").getAsString();
                }
                if (variant != null && variant.isEmpty()) {
                    variant = null;
                }
                if (variant != null && source.equals("MASTER")) {
                    source = null;
                }
                yield new SoundValue(name, pitch, volume, variant, source);
            }
            case POTION -> {
                String name = valueObject.get("potion").getAsString();
                int amplifier = valueObject.get("amplifier").getAsInt();
                int duration = valueObject.get("duration").getAsInt();
                yield new PotionValue(name, amplifier, duration);
            }
            case PARTICLE -> {
                String name = valueObject.get("particle_type").getAsString();
                String material = null;
                double spreadH = 0;
                double spreadV = 0;
                double motionX = 0;
                double motionY = 0;
                double motionZ = 0;
                int count = 1;
                int color = 0xFF0000;
                double size = 1;
                if (valueObject.has("material")) {
                    material = valueObject.get("material").getAsString();
                }
                if (valueObject.has("first_spread")) {
                    spreadH = valueObject.get("first_spread").getAsDouble();
                    spreadV = valueObject.get("second_spread").getAsDouble();
                }
                if (valueObject.has("x_motion")) {
                    motionX = valueObject.get("x_motion").getAsDouble();
                    motionY = valueObject.get("y_motion").getAsDouble();
                    motionZ = valueObject.get("z_motion").getAsDouble();
                }
                if (valueObject.has("count")) {
                    count = valueObject.get("count").getAsInt();
                }
                if (valueObject.has("color")) {
                    color = valueObject.get("color").getAsInt();
                }
                if (valueObject.has("size")) {
                    size = valueObject.get("size").getAsDouble();
                }
                yield new ParticleValue(name, material, spreadH, spreadV, motionX, motionY, motionZ, count, color, size);
            }
            case ENUM -> new EnumValue(valueObject.get("enum").getAsString());
        };
    }

    public static void write(Writer writer, List<Handler> handlers) throws IOException {
        for (Handler handler : handlers) {
            writeHandler(writer, handler);
            writer.write("\n");
        }
    }

    private static void writeHandler(Writer writer, Handler handler) throws IOException {
        if (handler.getType() != HandlerType.FUNCTION) {
            writer.write(handler.getType().getName());
            writer.write(" ");
        }
        writeIdentifier(writer, handler.getName(), false);
        writer.write(" {\n");
        for (Operation operation : handler.getOperations()) {
            writeOperation(writer, operation, 1);
        }
        writer.write("}\n");
    }

    private static void writeIdentifier(Writer writer, String name, boolean force) throws IOException {
        if (force || name.isEmpty() || !Lexer.isLiteralStart(name.charAt(0))) {
            force = true;
        } else {
            for (char ch : name.toCharArray()) {
                if (!Lexer.isLiteral(ch)) {
                    force = true;
                    break;
                }
            }
        }
        if (force) {
            writer.write("`" + name.replace("\n", "\\n").replace("\\", "\\\\").replace("`", "\\`") + "`");
        } else {
            writer.write(name);
        }
    }

    private static void writeOperation(Writer writer, Operation operation, int level) throws IOException {
        for (int i = 0; i < level; ++i) {
            writer.write("    ");
        }
        if (operation.hasDelegate()) {
            writer.write(operation.getName());
            if (operation.isInverted()) {
                writer.write(" not");
            }
            writer.write(" ");
            writer.write(operation.getDelegate());
        } else {
            if (operation.isInverted()) {
                writer.write("not ");
            }
            writer.write(operation.getName());
        }
        if (operation.hasSelector()) {
            writer.write("<");
            writer.write(operation.getSelector());
            writer.write(">");
        }
        Map<String, Value> arguments = operation.getArguments();
        if (!arguments.isEmpty()) {
            writer.write("(");
            int i = 0;
            for (Map.Entry<String, Value> argument : arguments.entrySet()) {
                writer.write(argument.getKey());
                writer.write("=");
                writeValue(writer, argument.getValue());
                if (i != arguments.size() - 1) {
                    writer.write(", ");
                }
                i += 1;
            }
            writer.write(")");
        }
        List<Operation> operations = operation.getOperations();
        if (!operations.isEmpty()) {
            writer.write(" {");
            writer.write("\n");
            for (Operation innerOperation : operations) {
                writeOperation(writer, innerOperation, level + 1);
            }
            for (int i = 0; i < level; ++i) {
                writer.write("    ");
            }
            writer.write("}");
        } else {
            writer.write(";");
        }
        writer.write("\n");
    }

    private static void writeValue(Writer writer, Value value) throws IOException {
        switch (value.getType()) {
            case NUMBER -> {
                NumberValue number = (NumberValue)value;
                if (number.isPlaceholder()) {
                    writer.write(number.getPlaceholder());
                } else {
                    writer.write(Double.toString(number.getNumber()));
                }
            }
            case TEXT -> {
                TextValue text = (TextValue)value;
                if (text.getTextParsing() != TextParsing.PLAIN) {
                    writer.write(text.getTextParsing().getPrefix());
                }
                writeString(writer, text.getText());
            }
            case ARRAY -> {
                ArrayValue array = (ArrayValue)value;
                writer.write("{ ");
                for (int i = 0; i < array.size(); ++i) {
                    writeValue(writer, array.get(i));
                    if (i != array.size() - 1) {
                        writer.write(", ");
                    }
                }
                if (!array.isEmpty()) {
                    writer.write(" ");
                }
                writer.write("}");
            }
            case VARIABLE -> {
                VariableValue variable = (VariableValue)value;
                boolean nonLocal = variable.getScope() != VariableScope.LOCAL;
                if (nonLocal) {
                    writer.write(variable.getScope().getPrefix());
                }
                writeIdentifier(writer, variable.getName(), nonLocal);
            }
            case GAME -> {
                GameValue game = (GameValue)value;
                writer.write("<");
                if (game.hasSelector()) {
                    writer.write(game.getSelector());
                }
                writer.write(">");
                writer.write(game.getName());
            }
            case LOCATION -> {
                LocationValue location = (LocationValue)value;
                writer.write("location{ ");
                writeNumber(writer, location.getX());
                writer.write(", ");
                writeNumber(writer, location.getY());
                writer.write(", ");
                writeNumber(writer, location.getZ());
                if (location.hasRotation()) {
                    writer.write(", ");
                    writeNumber(writer, location.getYaw());
                    writer.write(", ");
                    writeNumber(writer, location.getPitch());
                }
                writer.write(" }");
            }
            case VECTOR -> {
                VectorValue vector = (VectorValue)value;
                writer.write("vector{ ");
                writeNumber(writer, vector.getX());
                writer.write(", ");
                writeNumber(writer, vector.getY());
                writer.write(", ");
                writeNumber(writer, vector.getZ());
                writer.write(" }");
            }
            case ITEM -> {
                ItemValue item = (ItemValue)value;
                writer.write("item");
                writer.write(item.getItem().valueToString());
            }
            case SOUND -> {
                SoundValue sound = (SoundValue)value;
                writer.write("sound{ ");
                writeString(writer, sound.getName());
                if (sound.getVolume() != 1) {
                    writer.write(", volume=");
                    writeNumber(writer, sound.getVolume());
                }
                if (sound.getPitch() != 1) {
                    writer.write(", pitch=");
                    writeNumber(writer, sound.getPitch());
                }
                if (sound.hasSource()) {
                    writer.write(", source=\"");
                    writer.write(sound.getSource());
                    writer.write("\"");
                }
                if (sound.hasVariant()) {
                    writer.write(", variant=\"");
                    writer.write(sound.getVariant());
                    writer.write("\"");
                }
                writer.write(" }");
            }
            case POTION -> {
                PotionValue potion = (PotionValue)value;
                writer.write("potion{ ");
                writeString(writer, potion.getName());
                if (potion.getAmplifier() != 0) {
                    writer.write(", amplifier=");
                    writer.write(Integer.toString(potion.getAmplifier()));
                }
                if (potion.getDuration() >= 0) {
                    writer.write(", duration=");
                    writer.write(Integer.toString(potion.getDuration()));
                }
                writer.write(" }");
            }
            case PARTICLE -> {
                ParticleValue particle = (ParticleValue)value;
                writer.write("particle{ ");
                writeString(writer, particle.getName());
                if (particle.hasMaterial()) {
                    writer.write(", material=\"");
                    writer.write(particle.getMaterial());
                    writer.write("\"");
                }
                if (particle.hasSpread()) {
                    writer.write(", spread={ ");
                    writeNumber(writer, particle.getSpreadH());
                    writer.write(", ");
                    writeNumber(writer, particle.getSpreadV());
                    writer.write(" }");
                }
                if (particle.hasMotion()) {
                    writer.write(", motion={ ");
                    writeNumber(writer, particle.getMotionX());
                    writer.write(", ");
                    writeNumber(writer, particle.getMotionY());
                    writer.write(", ");
                    writeNumber(writer, particle.getMotionZ());
                    writer.write(" }");
                }
                if (particle.getCount() != 1) {
                    writer.write(", count=");
                    writer.write(Integer.toString(particle.getCount()));
                }
                if (particle.getColor() != 0xFF0000) {
                    writer.write(String.format(", color=#%06X", particle.getColor()));
                }
                if (particle.getSize() != 1) {
                    writer.write(", size=");
                    writeNumber(writer, particle.getSize());
                }
                writer.write(" }");
            }
            case ENUM -> {
                EnumValue enumValue = (EnumValue)value;
                writer.write("'");
                writer.write(enumValue.getName().replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\t", "\\t"));
                writer.write("'");
            }
        }
    }

    private static void writeString(Writer writer, String string) throws IOException {
        writer.write("\"");
        writer.write(string.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t"));
        writer.write("\"");
    }

    private static void writeNumber(Writer writer, double number) throws IOException {
        writer.write(Double.toString((double)Math.round(number * 1000) / 1000));
    }
}
