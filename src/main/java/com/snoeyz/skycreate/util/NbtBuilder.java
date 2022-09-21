package com.snoeyz.skycreate.util;

import net.minecraft.nbt.CompoundTag;

public class NbtBuilder {
    private CompoundTag tag;

    private NbtBuilder() {
        tag = new CompoundTag();
    }

    public static NbtBuilder builder() {
        return new NbtBuilder();
    }

    public NbtBuilder putString(String name, String val) {
        tag.putString(name, val);
        return this;
    }

    public CompoundTag build() {
        return tag;
    }
}
