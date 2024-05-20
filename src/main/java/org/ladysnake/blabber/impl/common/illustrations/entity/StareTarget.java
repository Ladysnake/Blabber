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
package org.ladysnake.blabber.impl.common.illustrations.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;
import org.ladysnake.blabber.impl.common.serialization.FailingOptionalFieldCodec;
import org.ladysnake.blabber.impl.common.serialization.OptionalSerialization;

import java.util.Optional;
import java.util.OptionalInt;

public record StareTarget(Optional<IllustrationAnchor> anchor, OptionalInt x,
                          OptionalInt y) {
    public static final Codec<StareTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FailingOptionalFieldCodec.of(IllustrationAnchor.CODEC, "anchor").forGetter(StareTarget::anchor),
            OptionalSerialization.optionalIntField("x").forGetter(StareTarget::x),
            OptionalSerialization.optionalIntField("y").forGetter(StareTarget::y)
    ).apply(instance, StareTarget::new));
    public static final StareTarget FOLLOW_MOUSE = new StareTarget(Optional.empty(), OptionalInt.empty(), OptionalInt.empty());

    public StareTarget(PacketByteBuf buf) {
        this(
                buf.readOptional(b -> b.readEnumConstant(IllustrationAnchor.class)),
                OptionalSerialization.readOptionalInt(buf),
                OptionalSerialization.readOptionalInt(buf)
        );
    }

    public static void writeToPacket(PacketByteBuf buf, StareTarget stareTarget) {
        buf.writeOptional(stareTarget.anchor(), PacketByteBuf::writeEnumConstant);
        OptionalSerialization.writeOptionalInt(buf, stareTarget.x());
        OptionalSerialization.writeOptionalInt(buf, stareTarget.y());
    }
}
