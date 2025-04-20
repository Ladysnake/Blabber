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
package org.ladysnake.blabber.impl.common.packets;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * Represents a list of dialogue choices which availability has changed
 */
public record ChoiceAvailabilityPayload(Map<String, Int2BooleanMap> updatedChoices) implements CustomPayload {
    public static final CustomPayload.Id<ChoiceAvailabilityPayload> ID = BlabberRegistrar.payloadId("choice_availability");
    public static final PacketCodec<ByteBuf, ChoiceAvailabilityPayload> PACKET_CODEC = PacketCodecs.map(
            (IntFunction<Map<String, Int2BooleanMap>>) HashMap::new,
            PacketCodecs.STRING,
            PacketCodecs.map(Int2BooleanOpenHashMap::new, PacketCodecs.VAR_INT, PacketCodecs.BOOLEAN)
    ).xmap(ChoiceAvailabilityPayload::new, ChoiceAvailabilityPayload::updatedChoices);

    public ChoiceAvailabilityPayload() {
        this(new HashMap<>());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void markUpdated(String stateKey, int choiceIndex, boolean newValue) {
        this.updatedChoices().computeIfAbsent(stateKey, s -> new Int2BooleanOpenHashMap()).put(choiceIndex, newValue);
    }
}
