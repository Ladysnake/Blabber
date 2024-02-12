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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.ladysnake.blabber.api.DialogueIllustration;
import org.ladysnake.blabber.api.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.FailingOptionalFieldCodec;

import java.util.Optional;

public record DialogueIllustrationNbtEntity(Identifier id, int x1, int y1, int x2, int y2, int size, float yOff, Optional<Integer> stareAtX, Optional<Integer> stareAtY, Optional<NbtCompound> data) implements DialogueIllustration {
    private static final Codec<DialogueIllustrationNbtEntity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(DialogueIllustrationNbtEntity::id),
            Codec.INT.fieldOf("x1").forGetter(DialogueIllustrationNbtEntity::x1),
            Codec.INT.fieldOf("y1").forGetter(DialogueIllustrationNbtEntity::y1),
            Codec.INT.fieldOf("x2").forGetter(DialogueIllustrationNbtEntity::x2),
            Codec.INT.fieldOf("y2").forGetter(DialogueIllustrationNbtEntity::y2),
            Codec.INT.fieldOf("size").forGetter(DialogueIllustrationNbtEntity::size),
            FailingOptionalFieldCodec.of(Codec.FLOAT, "y_offset", 0.0f).forGetter(DialogueIllustrationNbtEntity::yOff),
            FailingOptionalFieldCodec.of(Codec.INT, "stare_at_x").forGetter(DialogueIllustrationNbtEntity::stareAtX),
            FailingOptionalFieldCodec.of(Codec.INT, "stare_at_y").forGetter(DialogueIllustrationNbtEntity::stareAtY),
            FailingOptionalFieldCodec.of(NbtCompound.CODEC, "data").forGetter(DialogueIllustrationNbtEntity::data)
    ).apply(instance, DialogueIllustrationNbtEntity::new));

    public static final DialogueIllustrationType<DialogueIllustrationNbtEntity> TYPE = new DialogueIllustrationType<>(
            CODEC,
            buf -> new DialogueIllustrationNbtEntity(
                    buf.readIdentifier(),
                    buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                    buf.readFloat(),
                    buf.readOptional(PacketByteBuf::readInt),
                    buf.readOptional(PacketByteBuf::readInt),
                    buf.readOptional(PacketByteBuf::readNbt)
            ),
            (buf, i) -> {
                buf.writeIdentifier(i.id());
                buf.writeInt(i.x1());
                buf.writeInt(i.y1());
                buf.writeInt(i.x2());
                buf.writeInt(i.y2());
                buf.writeInt(i.size());
                buf.writeFloat(i.yOff());
                buf.writeOptional(i.stareAtX(), PacketByteBuf::writeInt);
                buf.writeOptional(i.stareAtY(), PacketByteBuf::writeInt);
                buf.writeOptional(i.data(), PacketByteBuf::writeNbt);
            }
    );

    @Override
    @Environment(EnvType.CLIENT)
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY, float tickDelta) {
        LivingEntity e = (LivingEntity) Registries.ENTITY_TYPE.get(id).create(MinecraftClient.getInstance().world);

        if (e == null) return; // Something went wrong creating the entity, so don't render.

        e.bodyYaw = 0.0f;
        e.headYaw = 0.0f;

        e.prevBodyYaw = e.bodyYaw;
        e.prevHeadYaw = e.headYaw;
        data.ifPresent(e::readNbt);

        int fakedMouseX = stareAtX.map(s -> s + x + (x1 + x2)/2).orElse(mouseX);
        int fakedMouseY = stareAtY.map(s -> s + y + (y1 + y2)/2).orElse(mouseY);
        InventoryScreen.drawEntity(context, x + x1, y + y2, size, fakedMouseX, fakedMouseY, e);
    }

    @Override
    public DialogueIllustrationType<?> getType() {
        return TYPE;
    }
}
