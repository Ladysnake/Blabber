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
import org.ladysnake.blabber.impl.common.BlabberRegistrar;
import org.ladysnake.blabber.impl.common.serialization.FailingOptionalFieldCodec;

import java.util.function.BiConsumer;
import java.util.function.Function;

@ApiStatus.Experimental
public class DialogueLayoutType<P extends DialogueLayout.Params> {
    public static final Codec<DialogueLayout<?>> CODEC = BlabberRegistrar.LAYOUT_REGISTRY.getCodec().dispatch(
            "type", DialogueLayout::type, DialogueLayoutType::getCodec
    );

    private final Codec<DialogueLayout<P>> codec;
    private final Function<PacketByteBuf, P> read;
    private final BiConsumer<PacketByteBuf, P> write;

    public DialogueLayoutType(Codec<P> paramsCodec, P defaultParams, Function<PacketByteBuf, P> read, BiConsumer<PacketByteBuf, P> write) {
        this.codec = RecordCodecBuilder.create(instance -> instance.group(
                FailingOptionalFieldCodec.of(paramsCodec, "params", defaultParams).forGetter(DialogueLayout::params)
        ).apply(instance, p -> new DialogueLayout<>(this, p)));
        this.read = read;
        this.write = write;
    }

    /**
     * @return A codec to serialize and deserialize this layout's parameters
     */
    public Codec<DialogueLayout<P>> getCodec() {
        return codec;
    }

    /**
     * Parses this type of DialogueIllustration from a packet. The data within should be everything the client needs to render this
     * @param buf the packet's data
     * @return a newly parsed DialogueIllustration corresponding to this type
     */
    @ApiStatus.Experimental
    public static <P extends DialogueLayout.Params> DialogueLayout<P> readFromPacket(PacketByteBuf buf) {
        @SuppressWarnings("unchecked") DialogueLayoutType<P> type = (DialogueLayoutType<P>) buf.readRegistryValue(BlabberRegistrar.LAYOUT_REGISTRY);
        assert type != null;
        return new DialogueLayout<>(type, type.read.apply(buf));
    }

    /**
     * Write the data this illustration needs to be drawn client-side to a packet
     * @param buf the packet to write to
     * @param toWrite the illustration to write
     */
    @ApiStatus.Experimental
    public static <P extends DialogueLayout.Params> void writeToPacket(PacketByteBuf buf, DialogueLayout<P> toWrite) {
        buf.writeRegistryValue(BlabberRegistrar.LAYOUT_REGISTRY, toWrite.type());
        toWrite.type().write.accept(buf, toWrite.params());
    }
}
