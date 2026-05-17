/*
 * Blabber
 * Copyright (C) 2022-2026 Ladysnake
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.ResolutionContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record UnavailableAction(UnavailableDisplay display, Optional<Component> message) {
    public static final Codec<UnavailableAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UnavailableDisplay.CODEC.fieldOf("display").forGetter(UnavailableAction::display),
            ComponentSerialization.CODEC.optionalFieldOf("message").forGetter(UnavailableAction::message)
    ).apply(instance, UnavailableAction::new));
    public static final StreamCodec<FriendlyByteBuf, UnavailableAction> PACKET_CODEC = StreamCodec.composite(
            UnavailableDisplay.PACKET_CODEC, UnavailableAction::display,
            ByteBufCodecs.optional(ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC), UnavailableAction::message,
            UnavailableAction::new
    );

    public UnavailableAction resolve(ResolutionContext context) throws CommandSyntaxException {
        Optional<Component> parsedMessage = message().isEmpty() ? Optional.empty() : Optional.of(ComponentUtils.resolve(context, message().get()));
        return new UnavailableAction(display(), parsedMessage);
    }
}
