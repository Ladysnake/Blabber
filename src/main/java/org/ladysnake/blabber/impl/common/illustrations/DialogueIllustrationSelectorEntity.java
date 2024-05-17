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
package org.ladysnake.blabber.impl.common.illustrations;

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

    public record Spec(String selector, IllustrationAnchor anchor, int x1, int y1, int x2, int y2,
                       int size, float yOff,
                       Optional<Integer> stareAtX, Optional<Integer> stareAtY) implements DialogueIllustrationEntity.Spec {
        private static final MapCodec<Spec> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("entity").forGetter(Spec::selector),
                Codecs.createStrictOptionalFieldCodec(IllustrationAnchor.CODEC, "anchor", IllustrationAnchor.TOP_LEFT).forGetter(Spec::anchor),
                Codec.INT.fieldOf("x1").forGetter(Spec::x1),
                Codec.INT.fieldOf("y1").forGetter(Spec::y1),
                Codec.INT.fieldOf("x2").forGetter(Spec::x2),
                Codec.INT.fieldOf("y2").forGetter(Spec::y2),
                Codec.INT.fieldOf("size").forGetter(Spec::size),
                Codecs.createStrictOptionalFieldCodec(Codec.FLOAT, "y_offset", 0.0f).forGetter(Spec::yOff),
                Codecs.createStrictOptionalFieldCodec(Codec.INT, "stare_at_x").forGetter(Spec::stareAtX),
                Codecs.createStrictOptionalFieldCodec(Codec.INT, "stare_at_y").forGetter(Spec::stareAtY)
        ).apply(instance, Spec::new));

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
                    buf.readOptional(PacketByteBuf::readInt),
                    buf.readOptional(PacketByteBuf::readInt)
            );
        }

        public void writeToBuffer(PacketByteBuf buf) {
            buf.writeString(selector());
            buf.writeEnumConstant(anchor());
            buf.writeInt(x1());
            buf.writeInt(y1());
            buf.writeInt(x2());
            buf.writeInt(y2());
            buf.writeInt(size());
            buf.writeFloat(yOff());
            buf.writeOptional(stareAtX(), PacketByteBuf::writeInt);
            buf.writeOptional(stareAtY(), PacketByteBuf::writeInt);
        }
    }
}
