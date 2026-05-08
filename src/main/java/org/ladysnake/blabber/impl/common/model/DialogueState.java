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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.impl.common.InstancedDialogueAction;
import org.ladysnake.blabber.impl.common.machine.ChoiceResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record DialogueState(
        Component text,
        List<String> illustrations,
        List<DialogueChoice> choices,
        Optional<InstancedDialogueAction<?>> action,
        StateType type
) implements ChoiceResult {
    public static final Codec<DialogueState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.optionalFieldOf("text", Component.empty()).forGetter(DialogueState::text),
            Codec.list(Codec.STRING).optionalFieldOf("illustrations", Collections.emptyList()).forGetter(DialogueState::illustrations),
            Codec.list(DialogueChoice.CODEC).optionalFieldOf("choices", List.of()).forGetter(DialogueState::choices),
            InstancedDialogueAction.CODEC.optionalFieldOf("action").forGetter(DialogueState::action),
            Codec.STRING.xmap(s -> Enum.valueOf(StateType.class, s.toUpperCase(Locale.ROOT)), Enum::name).optionalFieldOf("type", StateType.DEFAULT).forGetter(DialogueState::type)
    ).apply(instance, DialogueState::new));
    public static final StreamCodec<FriendlyByteBuf, DialogueState> PACKET_CODEC = StreamCodec.composite(
            ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC, DialogueState::text,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), DialogueState::illustrations,
            ByteBufCodecs.collection(ArrayList::new, DialogueChoice.PACKET_CODEC), DialogueState::choices,
            // not writing the action, the client most likely does not need to know about it
            StreamCodec.ofMember((value, buf) -> {}, buf -> Optional.empty()), DialogueState::action,
            StateType.PACKET_CODEC, DialogueState::type,
            DialogueState::new
    );

    public String getNextState(int choice) {
        return this.choices.get(choice).next();
    }

    public DialogueState parseText(@Nullable CommandSourceStack source, @Nullable Entity sender) throws CommandSyntaxException {
        List<DialogueChoice> parsedChoices = new ArrayList<>(choices().size());
        for (DialogueChoice choice : choices()) {
            parsedChoices.add(choice.parseText(source, sender));
        }

        return new DialogueState(
                ComponentUtils.updateForEntity(source, text(), sender, 0),
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
