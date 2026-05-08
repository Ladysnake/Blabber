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
package org.ladysnake.blabber.impl.common.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;

public record ChoiceSelectionPayload(byte selectedChoice) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ChoiceSelectionPayload> ID = BlabberRegistrar.payloadId("choice_selection");
    public static final StreamCodec<ByteBuf, ChoiceSelectionPayload> PACKET_CODEC = ByteBufCodecs.BYTE.map(ChoiceSelectionPayload::new, ChoiceSelectionPayload::selectedChoice);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
