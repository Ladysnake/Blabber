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
import com.mojang.datafixers.util.Either;
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
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.DialogueIllustration;
import org.ladysnake.blabber.api.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.FailingOptionalFieldCodec;

import java.util.Optional;

public record DialogueIllustrationSelectorEntity(Either<String, LivingEntity> selector, int x1, int y1, int x2, int y2,
                                                 int size, float yOff,
                                                 Optional<Integer> stareAtX, Optional<Integer> stareAtY) implements DialogueIllustration {
    private static final Codec<DialogueIllustrationSelectorEntity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("entity")
                    .xmap(Either::<String, LivingEntity>left, e -> e.map(s -> s, Entity::getUuidAsString))
                    .forGetter(DialogueIllustrationSelectorEntity::selector),
            Codec.INT.fieldOf("x1").forGetter(DialogueIllustrationSelectorEntity::x1),
            Codec.INT.fieldOf("y1").forGetter(DialogueIllustrationSelectorEntity::y1),
            Codec.INT.fieldOf("x2").forGetter(DialogueIllustrationSelectorEntity::x2),
            Codec.INT.fieldOf("y2").forGetter(DialogueIllustrationSelectorEntity::y2),
            Codec.INT.fieldOf("size").forGetter(DialogueIllustrationSelectorEntity::size),
            FailingOptionalFieldCodec.of(Codec.FLOAT, "y_offset", 0.0f).forGetter(DialogueIllustrationSelectorEntity::yOff),
            FailingOptionalFieldCodec.of(Codec.INT, "stare_at_x").forGetter(DialogueIllustrationSelectorEntity::stareAtX),
            FailingOptionalFieldCodec.of(Codec.INT, "stare_at_y").forGetter(DialogueIllustrationSelectorEntity::stareAtY)
    ).apply(instance, DialogueIllustrationSelectorEntity::new));

    public static final DialogueIllustrationType<DialogueIllustrationSelectorEntity> TYPE = new DialogueIllustrationType<>(
            CODEC,
            buf -> {
                Either<String, Integer> either = buf.readEither(PacketByteBuf::readString, PacketByteBuf::readInt);

                return new DialogueIllustrationSelectorEntity(
                        either.map(Either::left, id -> {
                            assert MinecraftClient.getInstance().world != null;
                            Entity e = MinecraftClient.getInstance().world.getEntityById(id);
                            return e != null && e.isLiving() ? Either.right(((LivingEntity) e)) : Either.left(id.toString());
                        }),
                        buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(),
                        buf.readOptional(PacketByteBuf::readInt),
                        buf.readOptional(PacketByteBuf::readInt)
                );
            },
            (buf, i) -> {
                buf.writeEither(i.selector(), PacketByteBuf::writeString, (b, e) -> b.writeInt(e.getId()));
                buf.writeInt(i.x1());
                buf.writeInt(i.y1());
                buf.writeInt(i.x2());
                buf.writeInt(i.y2());
                buf.writeInt(i.size());
                buf.writeFloat(i.yOff());
                buf.writeOptional(i.stareAtX(), PacketByteBuf::writeInt);
                buf.writeOptional(i.stareAtY(), PacketByteBuf::writeInt);
            }
    );

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY, float tickDelta) {
        selector.ifRight(e -> {
            int fakedMouseX = stareAtX.map(s -> s + x + (x1 + x2)/2).orElse(mouseX);
            int fakedMouseY = stareAtY.map(s -> s + y + (y1 + y2)/2).orElse(mouseY);
            InventoryScreen.drawEntity(context, x + x1, y + y1, size, fakedMouseX, fakedMouseY, e);
        });
    }

    @Override
    public DialogueIllustrationType<?> getType() {
        return TYPE;
    }

    @Override
    public DialogueIllustrationSelectorEntity parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) {
        if (source == null) {
            return this;
        }

        Optional<String> selector = this.selector.left();
        if (selector.isPresent()) {
            try {
                EntitySelector entitySelector = new EntitySelectorReader(new StringReader(selector.get())).read();
                Entity e = entitySelector.getEntity(source);
                if (!e.isLiving()) {
                    return this;
                } else {
                    return new DialogueIllustrationSelectorEntity(
                            Either.right((LivingEntity) e),
                            this.x1, this.y1, this.x2, this.y2, this.size, this.yOff, this.stareAtX, this.stareAtY
                    );
                }
            } catch (CommandSyntaxException e) {
                // Stuff went bad, cancel. This can happen if there's no interlocutor or something.
                return this;
            }
        } else {
            return this;
        }
    }
}
