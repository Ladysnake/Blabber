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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record DialogueChoice(Text text, List<String> illustrations, String next, Optional<DialogueChoiceCondition> condition) {
    public static final Codec<DialogueChoice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextCodecs.CODEC.fieldOf("text").forGetter(DialogueChoice::text),
            Codec.list(Codec.STRING).optionalFieldOf("illustrations", Collections.emptyList()).forGetter(DialogueChoice::illustrations),
            Codec.STRING.fieldOf("next").forGetter(DialogueChoice::next),
            DialogueChoiceCondition.CODEC.optionalFieldOf("only_if").forGetter(DialogueChoice::condition)
    ).apply(instance, DialogueChoice::new));
    public static final PacketCodec<PacketByteBuf, DialogueChoice> PACKET_CODEC = PacketCodec.tuple(
            TextCodecs.PACKET_CODEC, DialogueChoice::text,
            PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING), DialogueChoice::illustrations,
            PacketCodecs.STRING, DialogueChoice::next,
            PacketCodecs.optional(DialogueChoiceCondition.PACKET_CODEC), DialogueChoice::condition,
            DialogueChoice::new
    );

    public DialogueChoice parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) throws CommandSyntaxException {
        Optional<DialogueChoiceCondition> parsedCondition = condition().isEmpty() ? Optional.empty() : Optional.of(condition().get().parseText(source, sender));
        return new DialogueChoice(Texts.parse(source, text(), sender, 0), illustrations(), next(), parsedCondition);
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(StringUtils.abbreviate(this.text.getString(), 20), this.next);
    }
}
