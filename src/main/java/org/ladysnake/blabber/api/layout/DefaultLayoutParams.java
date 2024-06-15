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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.ladysnake.blabber.impl.common.serialization.FailingOptionalFieldCodec;

import java.util.Optional;

@ApiStatus.Experimental
public record DefaultLayoutParams(Optional<Margins> mainTextMargins) implements DialogueLayout.Params {
    public static final DefaultLayoutParams DEFAULT = new DefaultLayoutParams(Optional.empty());
    public static final Codec<DefaultLayoutParams> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FailingOptionalFieldCodec.of(Margins.CODEC, "main_text_margins").forGetter(DefaultLayoutParams::mainTextMargins)
    ).apply(instance, DefaultLayoutParams::new));

    public DefaultLayoutParams(PacketByteBuf buf) {
        this(buf.readOptional(Margins::new));
    }

    public Margins getMainTextMargins() {
        return this.mainTextMargins.orElse(Margins.NONE);
    }

    public static void writeToPacket(PacketByteBuf buf, DefaultLayoutParams params) {
        buf.writeOptional(params.mainTextMargins(), Margins::writeToPacket);
    }
}
