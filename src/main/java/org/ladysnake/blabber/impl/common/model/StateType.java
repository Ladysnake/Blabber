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
package org.ladysnake.blabber.impl.common.model;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

public enum StateType {
    DEFAULT(true), END_DIALOGUE(false), ASK_CONFIRMATION(false);

    public static final PacketCodec<PacketByteBuf, StateType> PACKET_CODEC = PacketCodec.ofStatic(
            PacketByteBuf::writeEnumConstant,
            buf -> buf.readEnumConstant(StateType.class)
    );

    private final boolean allowsIllustrations;

    StateType(boolean allowsIllustrations) {
        this.allowsIllustrations = allowsIllustrations;
    }

    public boolean allowsIllustrations() {
        return this.allowsIllustrations;
    }
}
