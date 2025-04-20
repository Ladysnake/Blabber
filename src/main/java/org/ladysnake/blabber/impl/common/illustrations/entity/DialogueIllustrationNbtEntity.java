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
package org.ladysnake.blabber.impl.common.illustrations.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import org.ladysnake.blabber.api.illustration.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;
import org.ladysnake.blabber.impl.common.serialization.EitherMapCodec;
import org.ladysnake.blabber.impl.common.serialization.MorePacketCodecs;
import org.ladysnake.blabber.impl.common.serialization.OptionalSerialization;

import java.util.Optional;

public record DialogueIllustrationNbtEntity(Identifier id, IllustrationAnchor anchor, int x, int y, int width, int height, int entitySize, float yOffset, StareTarget stareAt, Optional<NbtCompound> data) implements DialogueIllustrationEntity {
    private static final MapCodec<DialogueIllustrationNbtEntity> CODEC_V0 = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(DialogueIllustrationNbtEntity::id),
            IllustrationAnchor.CODEC.optionalFieldOf("anchor", IllustrationAnchor.TOP_LEFT).forGetter(DialogueIllustrationNbtEntity::anchor),
            Codec.INT.fieldOf("x1").forGetter(DialogueIllustrationNbtEntity::x),
            Codec.INT.fieldOf("y1").forGetter(DialogueIllustrationNbtEntity::y),
            Codec.INT.fieldOf("x2").forGetter(s -> s.x() + s.width()),
            Codec.INT.fieldOf("y2").forGetter(s -> s.y() + s.height()),
            Codec.INT.fieldOf("size").forGetter(DialogueIllustrationNbtEntity::entitySize),
            Codec.FLOAT.optionalFieldOf("y_offset", 0.0f).forGetter(DialogueIllustrationNbtEntity::yOffset),
            OptionalSerialization.optionalIntField("stare_at_x").forGetter(s -> s.stareAt().x()),
            OptionalSerialization.optionalIntField("stare_at_y").forGetter(s -> s.stareAt().y()),
            NbtCompound.CODEC.optionalFieldOf("data").forGetter(DialogueIllustrationNbtEntity::data)
    ).apply(instance, (id, anchor, x1, y1, x2, y2, size, yOff, stareAtX, stareAtY, data) -> {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        return new DialogueIllustrationNbtEntity(id, anchor, minX, minY, maxX - minX, maxY - minY, size, yOff, new StareTarget(Optional.empty(), stareAtX, stareAtY), data);
    }));
    private static final MapCodec<DialogueIllustrationNbtEntity> CODEC_V1 = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(DialogueIllustrationNbtEntity::id),
            IllustrationAnchor.CODEC.optionalFieldOf("anchor", IllustrationAnchor.TOP_LEFT).forGetter(DialogueIllustrationNbtEntity::anchor),
            Codec.INT.fieldOf("x").forGetter(DialogueIllustrationNbtEntity::x),
            Codec.INT.fieldOf("y").forGetter(DialogueIllustrationNbtEntity::y),
            Codec.INT.fieldOf("width").forGetter(DialogueIllustrationNbtEntity::width),
            Codec.INT.fieldOf("height").forGetter(DialogueIllustrationNbtEntity::height),
            Codec.INT.fieldOf("entity_size").forGetter(DialogueIllustrationNbtEntity::entitySize),
            Codec.FLOAT.optionalFieldOf("y_offset", 0.0f).forGetter(DialogueIllustrationNbtEntity::yOffset),
            StareTarget.CODEC.optionalFieldOf("stare_at", StareTarget.FOLLOW_MOUSE).forGetter(DialogueIllustrationNbtEntity::stareAt),
            NbtCompound.CODEC.optionalFieldOf("data").forGetter(DialogueIllustrationNbtEntity::data)
    ).apply(instance, DialogueIllustrationNbtEntity::new));
    public static final MapCodec<DialogueIllustrationNbtEntity> CODEC = EitherMapCodec.alternatively(CODEC_V0, CODEC_V1);
    public static final PacketCodec<PacketByteBuf, DialogueIllustrationNbtEntity> PACKET_CODEC = MorePacketCodecs.tuple(
            Identifier.PACKET_CODEC, DialogueIllustrationNbtEntity::id,
            IllustrationAnchor.PACKET_CODEC, DialogueIllustrationNbtEntity::anchor,
            PacketCodecs.VAR_INT, DialogueIllustrationNbtEntity::x,
            PacketCodecs.VAR_INT, DialogueIllustrationNbtEntity::y,
            PacketCodecs.VAR_INT, DialogueIllustrationNbtEntity::width,
            PacketCodecs.VAR_INT, DialogueIllustrationNbtEntity::height,
            PacketCodecs.VAR_INT, DialogueIllustrationNbtEntity::entitySize,
            PacketCodecs.FLOAT, DialogueIllustrationNbtEntity::yOffset,
            StareTarget.PACKET_CODEC, DialogueIllustrationNbtEntity::stareAt,
            PacketCodecs.NBT_COMPOUND.collect(PacketCodecs::optional), DialogueIllustrationNbtEntity::data,
            DialogueIllustrationNbtEntity::new
    );
    public static final DialogueIllustrationType<DialogueIllustrationNbtEntity> TYPE = new DialogueIllustrationType<>(CODEC, PACKET_CODEC);

    @Override
    public DialogueIllustrationType<?> getType() {
        return TYPE;
    }
}
