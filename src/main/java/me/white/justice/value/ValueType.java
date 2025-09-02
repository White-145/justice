package me.white.justice.value;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;

import java.io.IOException;
import java.util.Base64;

public enum ValueType {
    NUMBER("number") {
        @Override
        public Value decompile(JsonObject object) {
            JsonPrimitive primitive = object.get("number").getAsJsonPrimitive();
            if (primitive.isString()) {
                return new NumberValue(primitive.getAsString());
            }
            return new NumberValue(primitive.getAsDouble());
        }
    },
    TEXT("text") {
        @Override
        public Value decompile(JsonObject object) {
            String text = object.get("text").getAsString();
            TextParsing parsing = TextParsing.byName(object.get("parsing").getAsString());
            if (parsing == null) {
                throw null; // invalid text parsing
            }
            return new TextValue(parsing, text);
        }
    },
    ARRAY("array"),
    VARIABLE("variable") {
        @Override
        public Value decompile(JsonObject object) {
            String name = object.get("variable").getAsString();
            VariableScope scope = VariableScope.byName(object.get("scope").getAsString());
            if (scope == null) {
                throw null; // invalid variable scope
            }
            return new VariableValue(scope, name);
        }
    },
    GAME("game_value") {
        @Override
        public Value decompile(JsonObject object) {
            String name = object.get("game_value").getAsString();
            String selector = object.get("selection").getAsString();
            if (selector.equals("null")) {
                return new GameValue(null, name);
            }
            selector = selector.substring(10, selector.length() - 2);
            return new GameValue(selector, name);
        }
    },
    LOCATION("location") {
        @Override
        public Value decompile(JsonObject object) {
            double x = object.get("x").getAsDouble();
            double y = object.get("y").getAsDouble();
            double z = object.get("z").getAsDouble();
            double yaw = object.get("yaw").getAsDouble();
            double pitch = object.get("pitch").getAsDouble();
            return new LocationValue(x, y, z, yaw, pitch);
        }
    },
    VECTOR("vector") {
        @Override
        public Value decompile(JsonObject object) {
            double x = object.get("x").getAsDouble();
            double y = object.get("y").getAsDouble();
            double z = object.get("z").getAsDouble();
            return new VectorValue(x, y, z);
        }
    },
    ITEM("item") {
        @Override
        public Value decompile(JsonObject object) {
            String serialized = object.get("item").getAsString();
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
            return new ItemValue((CompoundTag)item);
        }
    },
    SOUND("sound") {
        @Override
        public Value decompile(JsonObject object) {
            String name = object.get("sound").getAsString();
            double pitch = object.get("pitch").getAsDouble();
            double volume = object.get("volume").getAsDouble();
            String variant = null;
            String source = null;
            if (object.has("variation")) {
                variant = object.get("variation").getAsString();
            }
            if (object.has("source")) {
                object.get("source").getAsString();
            }
            if (variant != null && variant.isEmpty()) {
                variant = null;
            }
            if (variant != null && source.equals("MASTER")) {
                source = null;
            }
            return new SoundValue(name, pitch, volume, variant, source);
        }
    },
    POTION("potion") {
        @Override
        public Value decompile(JsonObject object) {
            String name = object.get("potion").getAsString();
            int amplifier = object.get("amplifier").getAsInt();
            int duration = object.get("duration").getAsInt();
            return new PotionValue(name, amplifier, duration);
        }
    },
    PARTICLE("particle") {
        @Override
        public Value decompile(JsonObject object) {
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
    ENUM("enum") {
        @Override
        public Value decompile(JsonObject object) {
            return new EnumValue(object.get("enum").getAsString());
        }
    };

    final String name;

    ValueType(String name) {
        this.name = name;
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

    public Value decompile(JsonObject object) {
        throw new UnsupportedOperationException();
    }
}
