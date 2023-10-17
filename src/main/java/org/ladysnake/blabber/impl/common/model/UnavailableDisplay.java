package org.ladysnake.blabber.impl.common.model;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum UnavailableDisplay implements StringIdentifiable {
    GRAYED_OUT("grayed_out"), HIDDEN("hidden");

    public static final Codec<UnavailableDisplay> CODEC = StringIdentifiable.createCodec(UnavailableDisplay::values);

    private final String id;

    UnavailableDisplay(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return this.id;
    }
}
