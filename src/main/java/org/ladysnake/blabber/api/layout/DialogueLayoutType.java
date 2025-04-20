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
package org.ladysnake.blabber.api.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.jetbrains.annotations.ApiStatus;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;

@ApiStatus.Experimental
public class DialogueLayoutType<P extends DialogueLayout.Params> {
    public static final Codec<DialogueLayout<?>> CODEC = BlabberRegistrar.LAYOUT_REGISTRY.getCodec().dispatch(
            "type", DialogueLayout::type, DialogueLayoutType::getCodec
    );
    public static final PacketCodec<RegistryByteBuf, DialogueLayout<?>> PACKET_CODEC = PacketCodecs.registryValue(BlabberRegistrar.LAYOUT_REGISTRY_KEY).dispatch(
            DialogueLayout::type, DialogueLayoutType::getPacketCodec
    );

    private final MapCodec<DialogueLayout<P>> codec;
    private final PacketCodec<? super RegistryByteBuf, DialogueLayout<P>> packetCodec;

    public DialogueLayoutType(Codec<P> paramsCodec, PacketCodec<? super RegistryByteBuf, P> paramsPacketCodec, P defaultParams) {
        this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                paramsCodec.optionalFieldOf("params", defaultParams).forGetter(DialogueLayout::params)
        ).apply(instance, p -> new DialogueLayout<>(this, p)));
        this.packetCodec = paramsPacketCodec.xmap(p -> new DialogueLayout<>(this, p), DialogueLayout::params);
    }

    /**
     * @return A codec to serialize and deserialize this layout's parameters
     */
    public MapCodec<DialogueLayout<P>> getCodec() {
        return codec;
    }

    public PacketCodec<? super RegistryByteBuf, DialogueLayout<P>> getPacketCodec() {
        return packetCodec;
    }
}
