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

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;
import org.ladysnake.blabber.impl.common.serialization.EitherMapCodec;
import org.ladysnake.blabber.impl.common.serialization.OptionalSerialization;

import java.util.Optional;

public class DialogueIllustrationSelectorEntity extends DialogueIllustrationEntity<DialogueIllustrationSelectorEntity.Spec> {
    // Need to have a MapCodecCodec here, otherwise it will deserialize differently
    public static final Codec<DialogueIllustrationSelectorEntity> CODEC = Spec.CODEC.xmap(DialogueIllustrationSelectorEntity::new, DialogueIllustrationSelectorEntity::spec).codec();

    public static final DialogueIllustrationType<DialogueIllustrationSelectorEntity> TYPE = new DialogueIllustrationType<>(
            CODEC,
            buf -> new DialogueIllustrationSelectorEntity(
                    new Spec(buf),
                    buf.readVarInt()
            ),
            (buf, i) -> {
                i.spec().writeToBuffer(buf);
                buf.writeVarInt(i.selectedEntityId);
            }
    );

    private static final int NO_ENTITY_FOUND = -1;

    private int selectedEntityId;

    public DialogueIllustrationSelectorEntity(Spec spec) {
        this(spec, NO_ENTITY_FOUND);
    }

    private DialogueIllustrationSelectorEntity(Spec spec, int selectedEntityId) {
        super(spec);
        this.selectedEntityId = selectedEntityId;
    }

    @Override
    protected @Nullable LivingEntity getRenderedEntity(World world) {
        if (this.selectedEntityId == -1) return null; // shortcut
        Entity e = world.getEntityById(this.selectedEntityId);
        return e instanceof LivingEntity living ? living : null;
    }

    @Override
    public DialogueIllustrationType<?> getType() {
        return TYPE;
    }

    @Override
    public DialogueIllustrationSelectorEntity parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) throws CommandSyntaxException {
        if (source != null) {
            EntitySelector entitySelector = new EntitySelectorReader(new StringReader(spec().selector())).read();
            Entity e = entitySelector.getEntity(source);
            if (e instanceof LivingEntity living) {
                this.selectedEntityId = living.getId();
            }
        }
        return this;
    }

    public record Spec(String selector, IllustrationAnchor anchor, int x, int y, int width, int height,
                       int entitySize, float yOffset,
                       StareTarget stareAt) implements DialogueIllustrationEntity.Spec {
        private static final MapCodec<Spec> CODEC_V0 = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("entity").forGetter(Spec::selector),
                Codecs.createStrictOptionalFieldCodec(IllustrationAnchor.CODEC, "anchor", IllustrationAnchor.TOP_LEFT).forGetter(Spec::anchor),
                Codec.INT.fieldOf("x1").forGetter(Spec::x),
                Codec.INT.fieldOf("y1").forGetter(Spec::y),
                Codec.INT.fieldOf("x2").forGetter(s -> s.x() + s.width()),
                Codec.INT.fieldOf("y2").forGetter(s -> s.y() + s.height()),
                Codec.INT.fieldOf("size").forGetter(Spec::entitySize),
                Codecs.createStrictOptionalFieldCodec(Codec.FLOAT, "y_offset", 0.0f).forGetter(Spec::yOffset),
                OptionalSerialization.optionalIntField("stare_at_x").forGetter(s -> s.stareAt().x()),
                OptionalSerialization.optionalIntField("stare_at_y").forGetter(s -> s.stareAt().y())
        ).apply(instance, (id, anchor, x1, y1, x2, y2, size, yOff, stareAtX, stareAtY) -> {
            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            int maxX = Math.max(x1, x2);
            int maxY = Math.max(y1, y2);
            return new Spec(id, anchor, minX, minY, maxX - minX, maxY - minY, size, yOff, new StareTarget(Optional.empty(), stareAtX, stareAtY));
        }));
        private static final MapCodec<Spec> CODEC_V1 = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("entity").forGetter(Spec::selector),
                Codecs.createStrictOptionalFieldCodec(IllustrationAnchor.CODEC, "anchor", IllustrationAnchor.TOP_LEFT).forGetter(Spec::anchor),
                Codec.INT.fieldOf("x").forGetter(Spec::x),
                Codec.INT.fieldOf("y").forGetter(Spec::y),
                Codec.INT.fieldOf("width").forGetter(Spec::width),
                Codec.INT.fieldOf("height").forGetter(Spec::height),
                Codec.INT.fieldOf("entity_size").forGetter(Spec::entitySize),
                Codecs.createStrictOptionalFieldCodec(Codec.FLOAT, "y_offset", 0.0f).forGetter(Spec::yOffset),
                Codecs.createStrictOptionalFieldCodec(StareTarget.CODEC, "stare_at", StareTarget.FOLLOW_MOUSE).forGetter(Spec::stareAt)
        ).apply(instance, Spec::new));
        public static final MapCodec<Spec> CODEC = EitherMapCodec.alternatively(CODEC_V0, CODEC_V1);

        public Spec(PacketByteBuf buf) {
            this(
                    buf.readString(),
                    buf.readEnumConstant(IllustrationAnchor.class),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readFloat(),
                    new StareTarget(buf)
            );
        }

        public void writeToBuffer(PacketByteBuf buf) {
            buf.writeString(selector());
            buf.writeEnumConstant(anchor());
            buf.writeInt(x());
            buf.writeInt(y());
            buf.writeInt(width());
            buf.writeInt(height());
            buf.writeInt(entitySize());
            buf.writeFloat(yOffset());
            StareTarget.writeToPacket(buf, stareAt());
        }
    }
}
