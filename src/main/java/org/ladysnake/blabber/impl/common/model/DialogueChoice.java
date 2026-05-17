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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record DialogueChoice(Component text, List<String> illustrations, String next, Optional<DialogueChoiceCondition> condition) {
    public static final Codec<DialogueChoice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("text").forGetter(DialogueChoice::text),
            Codec.list(Codec.STRING).optionalFieldOf("illustrations", Collections.emptyList()).forGetter(DialogueChoice::illustrations),
            Codec.STRING.fieldOf("next").forGetter(DialogueChoice::next),
            DialogueChoiceCondition.CODEC.optionalFieldOf("only_if").forGetter(DialogueChoice::condition)
    ).apply(instance, DialogueChoice::new));
    public static final StreamCodec<FriendlyByteBuf, DialogueChoice> PACKET_CODEC = StreamCodec.composite(
            ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC, DialogueChoice::text,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), DialogueChoice::illustrations,
            ByteBufCodecs.STRING_UTF8, DialogueChoice::next,
            ByteBufCodecs.optional(DialogueChoiceCondition.PACKET_CODEC), DialogueChoice::condition,
            DialogueChoice::new
    );

    public DialogueChoice resolve(ResolutionContext context) throws CommandSyntaxException {
        Optional<DialogueChoiceCondition> parsedCondition = condition().isEmpty() ? Optional.empty() : Optional.of(condition().get().resolve(context));
        return new DialogueChoice(ComponentUtils.resolve(context, text()), illustrations(), next(), parsedCondition);
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(StringUtils.abbreviate(this.text.getString(), 20), this.next);
    }
}
