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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.DialogueIllustration;
import org.ladysnake.blabber.api.DialogueIllustrationType;

import java.util.Optional;

public class DialogueIllustrationSelectorEntity implements DialogueIllustration {
    public static final Codec<DialogueIllustrationSelectorEntity> CODEC = Spec.CODEC.xmap(DialogueIllustrationSelectorEntity::new, DialogueIllustrationSelectorEntity::spec);

    public static final DialogueIllustrationType<DialogueIllustrationSelectorEntity> TYPE = new DialogueIllustrationType<>(
            CODEC,
            buf -> new DialogueIllustrationSelectorEntity(
                    new Spec(buf),
                    buf.readVarInt()
            ),
            (buf, i) -> {
                i.spec().writeToBuffer(buf);
                buf.writeInt(i.selectedEntityId);
            }
    );
    public static final int NO_ENTITY_FOUND = -1;

    private final Spec spec;
    private int selectedEntityId;
    private transient @Nullable LivingEntity selectedEntity;

    public DialogueIllustrationSelectorEntity(Spec spec) {
        this(spec, NO_ENTITY_FOUND);
    }

    private DialogueIllustrationSelectorEntity(Spec spec, int selectedEntityId) {
        this.spec = spec;
        this.selectedEntityId = selectedEntityId;
    }

    public Spec spec() {
        return spec;
    }

    private @Nullable LivingEntity findEntity() {
        if (this.selectedEntityId == -1) return null; // shortcut
        assert MinecraftClient.getInstance().world != null;
        Entity e = MinecraftClient.getInstance().world.getEntityById(this.selectedEntityId);
        return e instanceof LivingEntity living ? living : null;
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY, float tickDelta) {
        LivingEntity e = this.selectedEntity == null && this.selectedEntityId != NO_ENTITY_FOUND ? this.selectedEntity = this.findEntity() : this.selectedEntity;

        if (this.selectedEntity != null) {
            int fakedMouseX = spec().stareAtX().map(s -> s + x + (spec().x1() + spec().x2())/2).orElse(mouseX);
            int fakedMouseY = spec().stareAtY().map(s -> s + y + (spec().y1() + spec().y2())/2).orElse(mouseY);
            InventoryScreen.drawEntity(context,
                    x + spec().x1(),
                    y + spec().y1(),
                    x + spec().x2(),
                    y + spec().y2(),
                    spec().size(),
                    spec().yOff(),
                    fakedMouseX,
                    fakedMouseY,
                    this.selectedEntity);
        }
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
                this.selectedEntity = living;
                this.selectedEntityId = living.getId();
            }
        }
        return this;
    }

    public record Spec(String selector, int x1, int y1, int x2, int y2,
                       int size, float yOff,
                       Optional<Integer> stareAtX, Optional<Integer> stareAtY) {
        private static final Codec<Spec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("entity").forGetter(Spec::selector),
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
