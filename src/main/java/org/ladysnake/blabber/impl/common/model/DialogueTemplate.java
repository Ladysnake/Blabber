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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.ResolutionContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.ladysnake.blabber.api.illustration.DialogueIllustration;
import org.ladysnake.blabber.api.illustration.DialogueIllustrationType;
import org.ladysnake.blabber.api.layout.DialogueLayout;
import org.ladysnake.blabber.api.layout.DialogueLayoutType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record DialogueTemplate(
        Optional<Component> name,
        String start,
        boolean unskippable,
        Map<String, DialogueState> states,
        Map<String, DialogueIllustration> illustrations,
        DialogueLayout<?> layout
) {
    public static final Codec<DialogueTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(DialogueTemplate::name),
            Codec.STRING.fieldOf("start_at").forGetter(DialogueTemplate::start),
            Codec.BOOL.optionalFieldOf("unskippable", false).forGetter(DialogueTemplate::unskippable),
            Codec.unboundedMap(Codec.STRING, DialogueState.CODEC).fieldOf("states").forGetter(DialogueTemplate::states),
            Codec.unboundedMap(Codec.STRING, DialogueIllustrationType.CODEC).optionalFieldOf("illustrations", Collections.emptyMap()).forGetter(DialogueTemplate::illustrations),
            DialogueLayoutType.CODEC.optionalFieldOf("layout", DialogueLayout.DEFAULT).forGetter(DialogueTemplate::layout)
    ).apply(instance, DialogueTemplate::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueTemplate> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC), DialogueTemplate::name,
            ByteBufCodecs.STRING_UTF8, DialogueTemplate::start,
            ByteBufCodecs.BOOL, DialogueTemplate::unskippable,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, DialogueState.PACKET_CODEC), DialogueTemplate::states,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, DialogueIllustrationType.PACKET_CODEC), DialogueTemplate::illustrations,
            DialogueLayoutType.PACKET_CODEC, DialogueTemplate::layout,
            DialogueTemplate::new
    );

    public DialogueTemplate resolve(ResolutionContext context) throws CommandSyntaxException {
        Map<String, DialogueState> parsedStates = new HashMap<>(states().size());

        for (Map.Entry<String, DialogueState> state : states().entrySet()) {
            parsedStates.put(state.getKey(), state.getValue().resolve(context));
        }

        Map<String, DialogueIllustration> parsedIllustrations = new HashMap<>(illustrations().size());
        for (Map.Entry<String, DialogueIllustration> illustration : illustrations().entrySet()) {
            parsedIllustrations.put(illustration.getKey(), illustration.getValue().resolve(context));
        }

        Optional<Component> resolvedName = name.isPresent() ? Optional.of(ComponentUtils.resolve(context, name.get())) : Optional.empty();

        return new DialogueTemplate(
                resolvedName,
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
