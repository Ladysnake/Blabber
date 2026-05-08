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
package org.ladysnake.blabber.api.illustration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;
import org.ladysnake.blabber.impl.common.illustrations.DialogueIllustrationCollection;

/**
 * A type of {@link DialogueIllustration}, used for registration and abstraction.
 *
 * @param <T> the DialogueIllustration type this type creates
 * @param codec a codec to serialize and deserialize this DialogueIllustration
 */
public record DialogueIllustrationType<T extends DialogueIllustration>(MapCodec<T> codec,
                                                                       StreamCodec<? super RegistryFriendlyByteBuf, T> packetCodec) {
    public static final Codec<DialogueIllustration> CODEC = Codec.recursive("illustration_type", self ->
            Codec.withAlternative(
                    BlabberRegistrar.ILLUSTRATION_REGISTRY.byNameCodec()
                            .dispatch("type", DialogueIllustration::getType, DialogueIllustrationType::codec),
                    Codec.list(self).xmap(DialogueIllustrationCollection::new, DialogueIllustrationCollection::elements)
            )
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueIllustration> PACKET_CODEC = ByteBufCodecs.registry(BlabberRegistrar.ILLUSTRATION_REGISTRY_KEY)
            .dispatch(DialogueIllustration::getType, DialogueIllustrationType::packetCodec);
}
