/*
 * Blabber
 * Copyright (C) 2022-2025 Ladysnake
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
package org.ladysnake.blabber.impl.common.illustrations;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.illustration.DialogueIllustration;
import org.ladysnake.blabber.api.illustration.DialogueIllustrationType;

import java.util.ArrayList;
import java.util.List;

public record DialogueIllustrationCollection(List<DialogueIllustration> elements) implements DialogueIllustration {
    private static final MapCodec<DialogueIllustrationCollection> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.list(DialogueIllustrationType.CODEC).fieldOf("elements").forGetter(DialogueIllustrationCollection::elements)
    ).apply(instance, DialogueIllustrationCollection::new));
    public static final PacketCodec<RegistryByteBuf, DialogueIllustrationCollection> PACKET_CODEC = DialogueIllustrationType.PACKET_CODEC.collect(PacketCodecs.toList())
            .xmap(DialogueIllustrationCollection::new, DialogueIllustrationCollection::elements);

    public static final DialogueIllustrationType<DialogueIllustrationCollection> TYPE = new DialogueIllustrationType<>(CODEC, PACKET_CODEC);

    @Override
    public DialogueIllustrationType<? extends DialogueIllustration> getType() {
        return TYPE;
    }

    @Override
    public DialogueIllustration parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) throws CommandSyntaxException {
        List<DialogueIllustration> parsedSub = new ArrayList<>(elements.size());
        for (DialogueIllustration illustration : elements) {
            parsedSub.add(illustration.parseText(source, sender));
        }
        return new DialogueIllustrationCollection(parsedSub);
    }
}
