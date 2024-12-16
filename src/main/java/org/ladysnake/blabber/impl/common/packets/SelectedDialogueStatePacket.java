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

import net.minecraftforge.network.NetworkEvent;
import net.minecraft.network.PacketByteBuf;
import java.util.function.Supplier;

public class SelectedDialogueStatePacket {
    private final String stateKey;

    public SelectedDialogueStatePacket(String stateKey) {
        this.stateKey = stateKey;
    }

    public SelectedDialogueStatePacket(PacketByteBuf buf) {
        this.stateKey = buf.readString();
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(this.stateKey);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Handle the packet
        });
        context.setPacketHandled(true);
    }

    public String getStateKey() {
        return stateKey;
    }
}
