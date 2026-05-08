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
package org.ladysnake.blabber.api.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.ApiStatus;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;

@ApiStatus.Experimental
public class DialogueLayoutType<P extends DialogueLayout.Params> {
    public static final Codec<DialogueLayout<?>> CODEC = BlabberRegistrar.LAYOUT_REGISTRY.byNameCodec().dispatch(
            "type", DialogueLayout::type, DialogueLayoutType::getCodec
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueLayout<?>> PACKET_CODEC = ByteBufCodecs.registry(BlabberRegistrar.LAYOUT_REGISTRY_KEY).dispatch(
            DialogueLayout::type, DialogueLayoutType::getPacketCodec
    );

    private final MapCodec<DialogueLayout<P>> codec;
    private final StreamCodec<? super RegistryFriendlyByteBuf, DialogueLayout<P>> packetCodec;

    public DialogueLayoutType(Codec<P> paramsCodec, StreamCodec<? super RegistryFriendlyByteBuf, P> paramsPacketCodec, P defaultParams) {
        this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                paramsCodec.optionalFieldOf("params", defaultParams).forGetter(DialogueLayout::params)
        ).apply(instance, p -> new DialogueLayout<>(this, p)));
        this.packetCodec = paramsPacketCodec.map(p -> new DialogueLayout<>(this, p), DialogueLayout::params);
    }

    /**
     * @return A codec to serialize and deserialize this layout's parameters
     */
    public MapCodec<DialogueLayout<P>> getCodec() {
        return codec;
    }

    public StreamCodec<? super RegistryFriendlyByteBuf, DialogueLayout<P>> getPacketCodec() {
        return packetCodec;
    }
}
