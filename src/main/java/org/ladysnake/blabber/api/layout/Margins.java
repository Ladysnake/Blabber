/*
 * Blabber
 * Copyright (C) 2022-2024 Ladysnake
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
package org.ladysnake.blabber.api.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.dynamic.Codecs;

public record Margins(int top, int right, int bottom, int left) {
    public static final Codec<Margins> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.createStrictOptionalFieldCodec(Codec.INT, "top", 0).forGetter(Margins::top),
            Codecs.createStrictOptionalFieldCodec(Codec.INT, "right", 0).forGetter(Margins::right),
            Codecs.createStrictOptionalFieldCodec(Codec.INT, "bottom", 0).forGetter(Margins::bottom),
            Codecs.createStrictOptionalFieldCodec(Codec.INT, "left", 0).forGetter(Margins::left)
    ).apply(instance, Margins::new));
    public static final Margins NONE = new Margins(0, 0, 0, 0);

    public static void writeToPacket(PacketByteBuf buf, Margins margins) {
        buf.writeVarInt(margins.top());
        buf.writeVarInt(margins.right());
        buf.writeVarInt(margins.bottom());
        buf.writeVarInt(margins.left());
    }

    public Margins(PacketByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }
}
