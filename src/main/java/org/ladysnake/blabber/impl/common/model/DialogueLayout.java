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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.ladysnake.blabber.Blabber;

// TODO change to a polymorphic type + registry to allow passing arbitrary data to the screen
public record DialogueLayout(Identifier type) {
    public static final Codec<DialogueLayout> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("type").forGetter(DialogueLayout::type)
    ).apply(instance, DialogueLayout::new));
    public static final Identifier CLASSIC_LAYOUT_ID = Blabber.id("classic");
    public static final Identifier RPG_LAYOUT_ID = Blabber.id("rpg");
    public static final DialogueLayout DEFAULT = new DialogueLayout(CLASSIC_LAYOUT_ID);

    public static void writeToPacket(PacketByteBuf buf, DialogueLayout layout) {
        buf.writeIdentifier(layout.type());
    }

    public DialogueLayout(PacketByteBuf buf) {
        this(buf.readIdentifier());
    }
}
