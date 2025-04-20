/*
 * Blabber
 * Copyright (C) 2022-2025 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package org.ladysnake.blabber.impl.common.model;

import com.mojang.serialization.Codec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.StringIdentifiable;

public enum IllustrationAnchor implements StringIdentifiable {
    /**
     * Starting at top-left, increasing towards bottom-right
     */
    TOP_LEFT("top_left"),
    /**
     * Starting at top-right, increasing towards bottom-left
     */
    TOP_RIGHT("top_right"),
    /**
     * Starting at bottom-left, increasing towards top-right
     */
    BOTTOM_LEFT("bottom_left"),
    /**
     * Starting at bottom-right, increasing towards top-left
     */
    BOTTOM_RIGHT("bottom_right"),
    /**
     * Starting at center, increasing towards bottom-right
     */
    CENTER("center"),
    /**
     * Right before the main text area starts
     */
    BEFORE_MAIN_TEXT("before_main_text"),
    SPOT_1("spot_1"),
    SPOT_2("spot_2");

    public static final Codec<IllustrationAnchor> CODEC = StringIdentifiable.createBasicCodec(IllustrationAnchor::values);
    public static final PacketCodec<PacketByteBuf, IllustrationAnchor> PACKET_CODEC = PacketCodec.ofStatic(
            PacketByteBuf::writeEnumConstant,
            buf -> buf.readEnumConstant(IllustrationAnchor.class)
    );

    private final String id;

    IllustrationAnchor(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return this.id;
    }
}
