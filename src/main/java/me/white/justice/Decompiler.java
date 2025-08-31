package me.white.justice;

import com.google.gson.*;
import me.white.justice.parser.Handler;
import me.white.justice.parser.HandlerType;
import me.white.justice.parser.Operation;
import me.white.justice.value.*;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;

import java.io.IOException;
import java.util.*;

public class Decompiler {
    public static List<Handler> decompile(String json) {
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
        List<Operation> operations = null;
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
            operations = new ArrayList<>();
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
                String variant = valueObject.get("variation").getAsString();
                if (variant.isEmpty()) {
                    variant = null;
                }
                String source = valueObject.get("source").getAsString();
                if (source.equals("MASTER")) {
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
}
