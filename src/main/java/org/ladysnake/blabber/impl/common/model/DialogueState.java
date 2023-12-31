/*
 * Blabber
 * Copyright (C) 2022-2023 Ladysnake
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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import net.minecraft.util.dynamic.Codecs;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.impl.common.InstancedDialogueAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record DialogueState(
        Text text,
        List<DialogueChoice> choices,
        Optional<InstancedDialogueAction<?>> action,
        ChoiceResult type
) {
    public static final Codec<DialogueState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            // Kinda optional, but we still want errors if you got it wrong >:(
            Codecs.createStrictOptionalFieldCodec(TextCodecs.CODEC, "text", Text.empty()).forGetter(DialogueState::text),
            Codecs.createStrictOptionalFieldCodec(Codec.list(DialogueChoice.CODEC), "choices", List.of()).forGetter(DialogueState::choices),
            Codecs.createStrictOptionalFieldCodec(InstancedDialogueAction.CODEC, "action").forGetter(DialogueState::action),
            Codecs.createStrictOptionalFieldCodec(Codec.STRING.xmap(s -> Enum.valueOf(ChoiceResult.class, s.toUpperCase(Locale.ROOT)), Enum::name), "type", ChoiceResult.DEFAULT).forGetter(DialogueState::type)
    ).apply(instance, DialogueState::new));

    public static void writeToPacket(PacketByteBuf buf, DialogueState state) {
        buf.writeText(state.text());
        buf.writeCollection(state.choices(), DialogueChoice::writeToPacket);
        buf.writeEnumConstant(state.type());
        // not writing the action, the client most likely does not need to know about it
    }

    public DialogueState(PacketByteBuf buf) {
        this(buf.readText(), buf.readList(DialogueChoice::new), Optional.empty(), buf.readEnumConstant(ChoiceResult.class));
    }

    public String getNextState(int choice) {
        return this.choices.get(choice).next();
    }

    public DialogueState parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) throws CommandSyntaxException {
        List<DialogueChoice> parsedChoices = new ArrayList<>();
        for (DialogueChoice choice : choices()) {
            parsedChoices.add(choice.parseText(source, sender));
        }
        return new DialogueState(
                Texts.parse(source, text(), sender, 0),
                parsedChoices,
                action(),
                type()
        );
    }

    @Override
    public String toString() {
        return "DialogueState{" +
                "text='" + StringUtils.abbreviate(text.getString(), 20) + '\'' +
                ", choices=" + choices +
                ", type=" + type
                + this.action().map(InstancedDialogueAction::toString).map(s -> ", action=" + s).orElse("")
                + '}';
    }
}
