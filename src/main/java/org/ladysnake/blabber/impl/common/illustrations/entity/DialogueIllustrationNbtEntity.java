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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.illustration.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;
import org.ladysnake.blabber.impl.common.serialization.EitherMapCodec;
import org.ladysnake.blabber.impl.common.serialization.OptionalSerialization;

import java.util.Optional;

public class DialogueIllustrationNbtEntity extends DialogueIllustrationEntity<DialogueIllustrationNbtEntity.Spec> {
    // Need to have a MapCodecCodec here, otherwise it will deserialize differently
    private static final Codec<DialogueIllustrationNbtEntity> CODEC = Spec.CODEC.xmap(DialogueIllustrationNbtEntity::new, DialogueIllustrationNbtEntity::spec).codec();
    public static final DialogueIllustrationType<DialogueIllustrationNbtEntity> TYPE = new DialogueIllustrationType<>(
            CODEC,
            buf -> new DialogueIllustrationNbtEntity(new Spec(
                    buf.readIdentifier(),
                    buf.readEnumConstant(IllustrationAnchor.class),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readFloat(),
                    new StareTarget(buf),
                    buf.readOptional(PacketByteBuf::readNbt)
            )),
            (buf, i) -> {
                buf.writeIdentifier(i.spec().id());
                buf.writeEnumConstant(i.spec().anchor());
                buf.writeInt(i.spec().x());
                buf.writeInt(i.spec().y());
                buf.writeInt(i.spec().width());
                buf.writeInt(i.spec().height());
                buf.writeInt(i.spec().entitySize());
                buf.writeFloat(i.spec().yOffset());
                StareTarget.writeToPacket(buf, i.spec().stareAt());
                buf.writeOptional(i.spec().data(), PacketByteBuf::writeNbt);
            }
    );

    public DialogueIllustrationNbtEntity(Spec spec) {
        super(spec);
    }

    @Override
    protected @Nullable LivingEntity getRenderedEntity(World world) {
        EntityType<?> entityType = Registries.ENTITY_TYPE.getOrEmpty(spec().id()).orElse(null);
        if (entityType == null) return null;

        if (entityType.create(world) instanceof LivingEntity living) {
            this.spec().data().ifPresent(living::readNbt);
            living.prevBodyYaw = living.bodyYaw = 0.0f;
            living.prevHeadYaw = living.headYaw = 0.0f;
            return living;
        }

        return null;
    }

    @Override
    public DialogueIllustrationType<?> getType() {
        return TYPE;
    }

    public record Spec(Identifier id, IllustrationAnchor anchor, int x, int y, int width, int height, int entitySize, float yOffset, StareTarget stareAt, Optional<NbtCompound> data) implements DialogueIllustrationEntity.Spec {
        private static final MapCodec<Spec> CODEC_V0 = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(Spec::id),
                Codecs.createStrictOptionalFieldCodec(IllustrationAnchor.CODEC, "anchor", IllustrationAnchor.TOP_LEFT).forGetter(Spec::anchor),
                Codec.INT.fieldOf("x1").forGetter(Spec::x),
                Codec.INT.fieldOf("y1").forGetter(Spec::y),
                Codec.INT.fieldOf("x2").forGetter(s -> s.x() + s.width()),
                Codec.INT.fieldOf("y2").forGetter(s -> s.y() + s.height()),
                Codec.INT.fieldOf("size").forGetter(Spec::entitySize),
                Codecs.createStrictOptionalFieldCodec(Codec.FLOAT, "y_offset", 0.0f).forGetter(Spec::yOffset),
                OptionalSerialization.optionalIntField("stare_at_x").forGetter(s -> s.stareAt().x()),
                OptionalSerialization.optionalIntField("stare_at_y").forGetter(s -> s.stareAt().y()),
                Codecs.createStrictOptionalFieldCodec(NbtCompound.CODEC, "data").forGetter(Spec::data)
        ).apply(instance, (id, anchor, x1, y1, x2, y2, size, yOff, stareAtX, stareAtY, data) -> {
            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            int maxX = Math.max(x1, x2);
            int maxY = Math.max(y1, y2);
            return new Spec(id, anchor, minX, minY, maxX - minX, maxY - minY, size, yOff, new StareTarget(Optional.empty(), stareAtX, stareAtY), data);
        }));
        private static final MapCodec<Spec> CODEC_V1 = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(Spec::id),
                Codecs.createStrictOptionalFieldCodec(IllustrationAnchor.CODEC, "anchor", IllustrationAnchor.TOP_LEFT).forGetter(Spec::anchor),
                Codec.INT.fieldOf("x").forGetter(Spec::x),
                Codec.INT.fieldOf("y").forGetter(Spec::y),
                Codec.INT.fieldOf("width").forGetter(Spec::width),
                Codec.INT.fieldOf("height").forGetter(Spec::height),
                Codec.INT.fieldOf("entity_size").forGetter(Spec::entitySize),
                Codecs.createStrictOptionalFieldCodec(Codec.FLOAT, "y_offset", 0.0f).forGetter(Spec::yOffset),
                Codecs.createStrictOptionalFieldCodec(StareTarget.CODEC, "stare_at", StareTarget.FOLLOW_MOUSE).forGetter(Spec::stareAt),
                Codecs.createStrictOptionalFieldCodec(NbtCompound.CODEC, "data").forGetter(Spec::data)
        ).apply(instance, Spec::new));
        public static final MapCodec<Spec> CODEC = EitherMapCodec.alternatively(CODEC_V0, CODEC_V1);
    }
}
