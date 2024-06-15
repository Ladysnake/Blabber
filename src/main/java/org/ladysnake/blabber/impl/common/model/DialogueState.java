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
import org.ladysnake.blabber.impl.common.InstancedDialogueAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record DialogueState(
        Text text,
        List<String> illustrations,
        List<DialogueChoice> choices,
        Optional<InstancedDialogueAction<?>> action,
        ChoiceResult type
) {
    public static final Codec<DialogueState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextCodecs.CODEC.optionalFieldOf("text", Text.empty()).forGetter(DialogueState::text),
            Codec.list(Codec.STRING).optionalFieldOf("illustrations", Collections.emptyList()).forGetter(DialogueState::illustrations),
            Codec.list(DialogueChoice.CODEC).optionalFieldOf("choices", List.of()).forGetter(DialogueState::choices),
            InstancedDialogueAction.CODEC.optionalFieldOf("action").forGetter(DialogueState::action),
            Codec.STRING.xmap(s -> Enum.valueOf(ChoiceResult.class, s.toUpperCase(Locale.ROOT)), Enum::name).optionalFieldOf("type", ChoiceResult.DEFAULT).forGetter(DialogueState::type)
    ).apply(instance, DialogueState::new));
    public static final PacketCodec<PacketByteBuf, DialogueState> PACKET_CODEC = PacketCodec.tuple(
            TextCodecs.PACKET_CODEC, DialogueState::text,
            PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING), DialogueState::illustrations,
            PacketCodecs.collection(ArrayList::new, DialogueChoice.PACKET_CODEC), DialogueState::choices,
            // not writing the action, the client most likely does not need to know about it
            PacketCodec.of((value, buf) -> {}, buf -> Optional.empty()), DialogueState::action,
            ChoiceResult.PACKET_CODEC, DialogueState::type,
            DialogueState::new
    );

    public String getNextState(int choice) {
        return this.choices.get(choice).next();
    }

    public DialogueState parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) throws CommandSyntaxException {
        List<DialogueChoice> parsedChoices = new ArrayList<>(choices().size());
        for (DialogueChoice choice : choices()) {
            parsedChoices.add(choice.parseText(source, sender));
        }

        return new DialogueState(
                Texts.parse(source, text(), sender, 0),
                illustrations(),
                parsedChoices,
                action(),
                type()
        );
    }

    @Override
    public String toString() {
        return "DialogueState{" +
                "text='" + StringUtils.abbreviate(text.getString(), 20) + '\'' +
                ", illustrations=" + illustrations +
                ", choices=" + choices +
                ", type=" + type
                + this.action().map(InstancedDialogueAction::toString).map(s -> ", action=" + s).orElse("")
                + '}';
    }
}
