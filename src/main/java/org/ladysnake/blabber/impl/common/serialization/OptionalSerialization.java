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
package org.ladysnake.blabber.impl.common.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.PacketByteBuf;

import java.util.Optional;
import java.util.OptionalInt;

public class OptionalSerialization {
    public static MapCodec<OptionalInt> optionalIntField(String name) {
        return Codec.INT.optionalFieldOf(name).xmap(v -> v.map(OptionalInt::of).orElseGet(OptionalInt::empty), v -> v.isPresent() ? Optional.of(v.getAsInt()) : Optional.empty());
    }

    public static OptionalInt readOptionalInt(PacketByteBuf buf) {
        if (buf.readBoolean()) {
            return OptionalInt.of(buf.readVarInt());
        }
        return OptionalInt.empty();
    }

    public static void writeOptionalInt(PacketByteBuf buf, OptionalInt value) {
        if (value.isPresent()) {
            buf.writeBoolean(true);
            buf.writeVarInt(value.getAsInt());
        } else {
            buf.writeBoolean(false);
        }
    }
}
