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
package org.ladysnake.blabber.impl.common.model;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record UnavailableAction(UnavailableDisplay display, Optional<Text> message) {
    public static final Codec<UnavailableAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UnavailableDisplay.CODEC.fieldOf("display").forGetter(UnavailableAction::display),
            TextCodecs.CODEC.optionalFieldOf("message").forGetter(UnavailableAction::message)
    ).apply(instance, UnavailableAction::new));
    public static final PacketCodec<PacketByteBuf, UnavailableAction> PACKET_CODEC = PacketCodec.tuple(
            UnavailableDisplay.PACKET_CODEC, UnavailableAction::display,
            PacketCodecs.optional(TextCodecs.PACKET_CODEC), UnavailableAction::message,
            UnavailableAction::new
    );

    public UnavailableAction parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) throws CommandSyntaxException {
        Optional<Text> parsedMessage = message().isEmpty() ? Optional.empty() : Optional.of(Texts.parse(source, message().get(), sender, 0));
        return new UnavailableAction(display(), parsedMessage);
    }
}
