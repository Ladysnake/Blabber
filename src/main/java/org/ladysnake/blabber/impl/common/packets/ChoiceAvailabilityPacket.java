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
package org.ladysnake.blabber.impl.common.packets;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import org.ladysnake.blabber.Blabber;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a list of dialogue choices which availability has changed
 */
public record ChoiceAvailabilityPacket(Map<String, Int2BooleanMap> updatedChoices) implements FabricPacket {
    public static final PacketType<ChoiceAvailabilityPacket> TYPE = PacketType.create(Blabber.id("choice_availability"), ChoiceAvailabilityPacket::new);

    public ChoiceAvailabilityPacket() {
        this(new HashMap<>());
    }

    public ChoiceAvailabilityPacket(PacketByteBuf buf) {
        this(buf.readMap(
                PacketByteBuf::readString,
                b -> b.readMap(Int2BooleanOpenHashMap::new, PacketByteBuf::readVarInt, PacketByteBuf::readBoolean)
        ));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeMap(
                this.updatedChoices(),
                PacketByteBuf::writeString,
                (b, updatedChoices) -> b.writeMap(updatedChoices, PacketByteBuf::writeVarInt, PacketByteBuf::writeBoolean)
        );
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public void markUpdated(String stateKey, int choiceIndex, boolean newValue) {
        this.updatedChoices().computeIfAbsent(stateKey, s -> new Int2BooleanOpenHashMap()).put(choiceIndex, newValue);
    }
}
