package me.white.justice.value;

import me.white.justice.CompilationException;
import me.white.justice.lexer.Lexer;
import net.querz.nbt.io.*;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;

public class ItemValue implements Value {
    private final CompoundTag item;

    public ItemValue(CompoundTag item) {
        this.item = item;
    }

    public static Value parse(Lexer lexer) throws CompilationException {
        Tag<?> tag;
        SNBTParser parser = new SNBTParser(lexer.getBuffer().substring(lexer.getPos()));
        try {
            tag = parser.parse(Tag.DEFAULT_MAX_DEPTH, true);
        } catch (ParseException e) {
            throw new CompilationException("Invalid item data: " + e.getMessage());
        }
        if (!(tag instanceof CompoundTag)) {
            throw new CompilationException("Invalid item data");
        }
        lexer.advanceReader(parser.getReadChars() - 1);
        return new ItemValue((CompoundTag)tag);
    }

    public CompoundTag getItem() {
        return item;
    }

    @Override
    public ValueType getType() {
        return ValueType.ITEM;
    }
}
