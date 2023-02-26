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
package io.github.ladysnake.blabber.impl.common;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record DialogueState(
    Text text,
    List<Choice> choices,
    Optional<InstancedDialogueAction<?>> action,
    ChoiceResult type
) {
    static Codec<DialogueState> codec(Codec<JsonElement> jsonCodec) {
        Codec<Text> textCodec = jsonCodec.xmap(Text.Serializer::fromJson, Text.Serializer::toJsonTree);

        return RecordCodecBuilder.create(instance -> instance.group(
            // Kinda optional, but we still want errors if you got it wrong >:(
            FailingOptionalFieldCodec.of("text", textCodec, Text.empty()).forGetter(DialogueState::text),
            FailingOptionalFieldCodec.of("choices", Codec.list(Choice.codec(textCodec)), List.of()).forGetter(DialogueState::choices),
            FailingOptionalFieldCodec.of("action", InstancedDialogueAction.CODEC).forGetter(DialogueState::action),
            FailingOptionalFieldCodec.of("type", Codec.STRING.xmap(s -> Enum.valueOf(ChoiceResult.class, s.toUpperCase(Locale.ROOT)), Enum::name), ChoiceResult.DEFAULT).forGetter(DialogueState::type)
        ).apply(instance, DialogueState::new));
    }

    public ImmutableList<Text> getAvailableChoices() {
        ImmutableList.Builder<Text> builder = ImmutableList.builder();
        for (Choice choice : this.choices) {
            builder.add(choice.text());
        }
        return builder.build();
    }

    public String getNextState(int choice) {
        return this.choices.get(choice).next();
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

    public record Choice(Text text, String next) {
        static Codec<Choice> codec(Codec<Text> textCodec) {
            return RecordCodecBuilder.create(instance -> instance.group(
                textCodec.fieldOf("text").forGetter(Choice::text),
                Codec.STRING.fieldOf("next").forGetter(Choice::next)
            ).apply(instance, Choice::new));
        }

        @Override
        public String toString() {
            return "%s -> %s".formatted(StringUtils.abbreviate(this.text.getString(), 20), this.next);
        }
    }
}
