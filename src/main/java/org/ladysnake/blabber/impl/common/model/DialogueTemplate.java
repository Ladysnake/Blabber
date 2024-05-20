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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.illustration.DialogueIllustration;
import org.ladysnake.blabber.api.illustration.DialogueIllustrationType;
import org.ladysnake.blabber.api.layout.DialogueLayout;
import org.ladysnake.blabber.api.layout.DialogueLayoutType;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record DialogueTemplate(String start, boolean unskippable, Map<String, DialogueState> states, Map<String, DialogueIllustration> illustrations, DialogueLayout<?> layout) {
    public static final Codec<DialogueTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("start_at").forGetter(DialogueTemplate::start),
            Codec.BOOL.optionalFieldOf("unskippable", false).forGetter(DialogueTemplate::unskippable),
            Codec.unboundedMap(Codec.STRING, DialogueState.CODEC).fieldOf("states").forGetter(DialogueTemplate::states),
            Codecs.createStrictOptionalFieldCodec(Codec.unboundedMap(Codec.STRING, DialogueIllustrationType.CODEC), "illustrations", Collections.emptyMap()).forGetter(DialogueTemplate::illustrations),
            Codecs.createStrictOptionalFieldCodec(DialogueLayoutType.CODEC, "layout", DialogueLayout.DEFAULT).forGetter(DialogueTemplate::layout)
    ).apply(instance, DialogueTemplate::new));

    public static void writeToPacket(PacketByteBuf buf, DialogueTemplate dialogue) {
        buf.writeString(dialogue.start());
        buf.writeBoolean(dialogue.unskippable());
        buf.writeMap(dialogue.states(), PacketByteBuf::writeString, DialogueState::writeToPacket);
        buf.writeMap(dialogue.illustrations(), PacketByteBuf::writeString, (b, i) -> {
            // Write the type, then the packet itself.
            b.writeRegistryValue(BlabberRegistrar.ILLUSTRATION_REGISTRY, i.getType());
            i.getType().writeToPacketUnsafe(b, i);
        });
        DialogueLayoutType.writeToPacket(buf, dialogue.layout());
    }

    public DialogueTemplate(PacketByteBuf buf) {
        this(buf.readString(), buf.readBoolean(), buf.readMap(PacketByteBuf::readString, DialogueState::new), buf.readMap(PacketByteBuf::readString, b -> {
            DialogueIllustrationType<?> type = b.readRegistryValue(BlabberRegistrar.ILLUSTRATION_REGISTRY);
            assert type != null;
            return type.readFromPacket(b);
        }), DialogueLayoutType.readFromPacket(buf));
    }

    public DialogueTemplate parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) throws CommandSyntaxException {
        Map<String, DialogueState> parsedStates = new HashMap<>(states().size());

        for (Map.Entry<String, DialogueState> state : states().entrySet()) {
            parsedStates.put(state.getKey(), state.getValue().parseText(source, sender));
        }

        Map<String, DialogueIllustration> parsedIllustrations = new HashMap<>(illustrations().size());
        for (Map.Entry<String, DialogueIllustration> illustration : illustrations().entrySet()) {
            parsedIllustrations.put(illustration.getKey(), illustration.getValue().parseText(source, sender));
        }

        return new DialogueTemplate(
                start(),
                unskippable(),
                parsedStates,
                parsedIllustrations,
                layout()
        );
    }

    @Override
    public String toString() {
        return "DialogueTemplate[start=%s, states=%s, illustrations=%s%s]".formatted(start, states, illustrations, unskippable ? " (unskippable)" : "");
    }
}
