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
package org.ladysnake.blabber.api.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record Margins(int top, int right, int bottom, int left) {
    public static final Codec<Margins> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("top", 0).forGetter(Margins::top),
            Codec.INT.optionalFieldOf("right", 0).forGetter(Margins::right),
            Codec.INT.optionalFieldOf("bottom", 0).forGetter(Margins::bottom),
            Codec.INT.optionalFieldOf("left", 0).forGetter(Margins::left)
    ).apply(instance, Margins::new));
    public static final PacketCodec<ByteBuf, Margins> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, Margins::top,
            PacketCodecs.VAR_INT, Margins::right,
            PacketCodecs.VAR_INT, Margins::bottom,
            PacketCodecs.VAR_INT, Margins::left,
            Margins::new
    );
    public static final Margins NONE = new Margins(0, 0, 0, 0);
}
