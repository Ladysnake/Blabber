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
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.Experimental
public record DefaultLayoutParams(Optional<Margins> mainTextMargins) implements DialogueLayout.Params {
    public static final DefaultLayoutParams DEFAULT = new DefaultLayoutParams(Optional.empty());
    public static final Codec<DefaultLayoutParams> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Margins.CODEC.optionalFieldOf("main_text_margins").forGetter(DefaultLayoutParams::mainTextMargins)
    ).apply(instance, DefaultLayoutParams::new));
    public static final PacketCodec<ByteBuf, DefaultLayoutParams> PACKET_CODEC = Margins.PACKET_CODEC.collect(PacketCodecs::optional).xmap(DefaultLayoutParams::new, DefaultLayoutParams::mainTextMargins);

    public Margins getMainTextMargins() {
        return this.mainTextMargins.orElse(Margins.NONE);
    }
}
