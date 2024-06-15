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

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntFunction;

public record DialogueListPayload(Set<Identifier> dialogueIds) implements CustomPayload {
    public static final CustomPayload.Id<DialogueListPayload> ID = BlabberRegistrar.payloadId("dialogue_list");
    public static final PacketCodec<ByteBuf, DialogueListPayload> PACKET_CODEC = Identifier.PACKET_CODEC.collect(PacketCodecs.toCollection((IntFunction<Set<Identifier>>) HashSet::new)).xmap(DialogueListPayload::new, DialogueListPayload::dialogueIds);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
