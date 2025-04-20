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
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;

public record ChoiceSelectionPayload(byte selectedChoice) implements CustomPayload {
    public static final CustomPayload.Id<ChoiceSelectionPayload> ID = BlabberRegistrar.payloadId("choice_selection");
    public static final PacketCodec<ByteBuf, ChoiceSelectionPayload> PACKET_CODEC = PacketCodecs.BYTE.xmap(ChoiceSelectionPayload::new, ChoiceSelectionPayload::selectedChoice);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
