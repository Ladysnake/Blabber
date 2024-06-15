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
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.illustration.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;
import org.ladysnake.blabber.impl.common.serialization.EitherMapCodec;
import org.ladysnake.blabber.impl.common.serialization.MorePacketCodecs;
import org.ladysnake.blabber.impl.common.serialization.OptionalSerialization;

import java.util.Optional;

public class DialogueIllustrationSelectorEntity implements DialogueIllustrationEntity {
    public static final MapCodec<DialogueIllustrationSelectorEntity> CODEC = Spec.CODEC.xmap(DialogueIllustrationSelectorEntity::new, DialogueIllustrationSelectorEntity::spec);
    public static final PacketCodec<PacketByteBuf, DialogueIllustrationSelectorEntity> PACKET_CODEC = Spec.PACKET_CODEC.xmap(DialogueIllustrationSelectorEntity::new, DialogueIllustrationSelectorEntity::spec);

    public static final DialogueIllustrationType<DialogueIllustrationSelectorEntity> TYPE = new DialogueIllustrationType<>(
            CODEC,
            PACKET_CODEC
    );

    private static final int NO_ENTITY_FOUND = -1;

    private final Spec spec;
    private int selectedEntityId;

    public DialogueIllustrationSelectorEntity(Spec spec) {
        this(spec, NO_ENTITY_FOUND);
    }

    private DialogueIllustrationSelectorEntity(Spec spec, int selectedEntityId) {
        this.spec = spec;
        this.selectedEntityId = selectedEntityId;
    }

    public LivingEntity getSelectedEntity(World world) {
        if (this.selectedEntityId == -1) return null; // shortcut
        Entity e = world.getEntityById(this.selectedEntityId);
        return e instanceof LivingEntity living ? living : null;
    }

    public Spec spec() {
        return spec;
    }

    @Override
    public StareTarget stareAt() {
        return this.spec.stareAt();
    }

    @Override
    public float yOffset() {
        return this.spec.yOffset();
    }

    @Override
    public int entitySize() {
        return this.spec.entitySize();
    }

    @Override
    public IllustrationAnchor anchor() {
        return this.spec.anchor();
    }

    @Override
    public int x() {
        return this.spec.x();
    }

    @Override
    public int y() {
        return this.spec.y();
    }

    @Override
    public int width() {
        return this.spec.width();
    }

    @Override
    public int height() {
        return this.spec.height();
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
                       StareTarget stareAt) {
        private static final MapCodec<Spec> CODEC_V0 = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("entity").forGetter(Spec::selector),
                IllustrationAnchor.CODEC.optionalFieldOf("anchor", IllustrationAnchor.TOP_LEFT).forGetter(Spec::anchor),
                Codec.INT.fieldOf("x1").forGetter(Spec::x),
                Codec.INT.fieldOf("y1").forGetter(Spec::y),
                Codec.INT.fieldOf("x2").forGetter(s -> s.x() + s.width()),
                Codec.INT.fieldOf("y2").forGetter(s -> s.y() + s.height()),
                Codec.INT.fieldOf("size").forGetter(Spec::entitySize),
                Codec.FLOAT.optionalFieldOf("y_offset", 0.0f).forGetter(Spec::yOffset),
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
                IllustrationAnchor.CODEC.optionalFieldOf("anchor", IllustrationAnchor.TOP_LEFT).forGetter(Spec::anchor),
                Codec.INT.fieldOf("x").forGetter(Spec::x),
                Codec.INT.fieldOf("y").forGetter(Spec::y),
                Codec.INT.fieldOf("width").forGetter(Spec::width),
                Codec.INT.fieldOf("height").forGetter(Spec::height),
                Codec.INT.fieldOf("entity_size").forGetter(Spec::entitySize),
                Codec.FLOAT.optionalFieldOf("y_offset", 0.0f).forGetter(Spec::yOffset),
                StareTarget.CODEC.optionalFieldOf("stare_at", StareTarget.FOLLOW_MOUSE).forGetter(Spec::stareAt)
        ).apply(instance, Spec::new));
        public static final MapCodec<Spec> CODEC = EitherMapCodec.alternatively(CODEC_V0, CODEC_V1);
        public static final PacketCodec<PacketByteBuf, Spec> PACKET_CODEC = MorePacketCodecs.tuple(
                PacketCodecs.STRING, Spec::selector,
                IllustrationAnchor.PACKET_CODEC, Spec::anchor,
                PacketCodecs.VAR_INT, Spec::x,
                PacketCodecs.VAR_INT, Spec::y,
                PacketCodecs.VAR_INT, Spec::width,
                PacketCodecs.VAR_INT, Spec::height,
                PacketCodecs.VAR_INT, Spec::entitySize,
                PacketCodecs.FLOAT, Spec::yOffset,
                StareTarget.PACKET_CODEC, Spec::stareAt,
                Spec::new
        );
    }
}
