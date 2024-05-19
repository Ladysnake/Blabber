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
package org.ladysnake.blabber.api;

import com.mojang.serialization.Codec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.ApiStatus;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;
import org.ladysnake.blabber.impl.common.illustrations.DialogueIllustrationCollection;
import org.ladysnake.blabber.impl.common.serialization.EitherCodecButGood;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A type of {@link DialogueIllustration}, used for registration and abstraction.
 * @param <T> the DialogueIllustration type this type creates
 */
public final class DialogueIllustrationType<T extends DialogueIllustration> {
    public static final Codec<DialogueIllustration> CODEC = Codecs.createRecursive("illustration_type", self ->
            EitherCodecButGood.alternatively(
                    BlabberRegistrar.ILLUSTRATION_REGISTRY.getCodec()
                            .dispatch("type", DialogueIllustration::getType, DialogueIllustrationType::getCodec),
                    Codec.list(self).xmap(DialogueIllustrationCollection::new, DialogueIllustrationCollection::elements)
            )
    );

    private final Codec<T> codec;
    private final Function<PacketByteBuf, T> read;
    private final BiConsumer<PacketByteBuf, T> write;

    public DialogueIllustrationType(Codec<T> codec, Function<PacketByteBuf, T> read, BiConsumer<PacketByteBuf, T> write) {
        this.codec = codec;
        this.read = read;
        this.write = write;
    }

    /**
     * @return A codec to serialize and deserialize this DialogueIllustration
     */
    public Codec<T> getCodec() {
        return codec;
    }

    /**
     * Parses this type of DialogueIllustration from a packet. The data within should be everything the client needs to render this
     * @param buf the packet's data
     * @return a newly parsed DialogueIllustration corresponding to this type
     */
    @ApiStatus.Experimental
    public T readFromPacket(PacketByteBuf buf) {
        return this.read.apply(buf);
    }

    /**
     * Write the data this illustration needs to be drawn client-side to a packet
     * @param buf the packet to write to
     * @param toWrite the illustration to write
     */
    @ApiStatus.Experimental
    public void writeToPacket(PacketByteBuf buf, T toWrite) {
        this.write.accept(buf, toWrite);
    }

    /**
     * Same as writeToPacket, but does an unchecked cast, for when the type information is lost somewhere.
     * Make sure it's safe.
     */
    @ApiStatus.Internal
    public void writeToPacketUnsafe(PacketByteBuf buf, DialogueIllustration illustration) {
        //noinspection unchecked
        this.writeToPacket(buf, (T) illustration);
    }
}
