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
package org.ladysnake.blabber.api.layout;

import com.mojang.serialization.Codec;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record DefaultLayoutParams() implements DialogueLayout.Params {
    public static final DefaultLayoutParams DEFAULT = new DefaultLayoutParams();
    public static final Codec<DefaultLayoutParams> CODEC = Codec.unit(() -> DefaultLayoutParams.DEFAULT);

    public DefaultLayoutParams(PacketByteBuf buf) {
        this();
    }

    public static void writeToPacket(PacketByteBuf buf, DefaultLayoutParams params) {
        // NO-OP
    }
}
