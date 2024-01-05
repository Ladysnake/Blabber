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
package org.ladysnake.blabber.impl.common;

import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import org.ladysnake.blabber.api.DialogueActionV2;

public record InstancedDialogueAction<A extends DialogueActionV2>(A action,
                                                                  Codec<A> codec) {
    public static final Codec<InstancedDialogueAction<?>> CODEC = BlabberRegistrar.ACTION_REGISTRY.getCodec()
            .dispatch("type", InstancedDialogueAction::codec, InstancedDialogueAction::xmap);

    private static <A extends DialogueActionV2> Codec<InstancedDialogueAction<A>> xmap(Codec<A> c) {
        return c.xmap(a -> new InstancedDialogueAction<>(a, c), InstancedDialogueAction::action);
    }

    @Override
    public String toString() {
        Identifier id = BlabberRegistrar.ACTION_REGISTRY.getId(this.codec);
        if (id == null) return "(unregistered action)";
        return id.toString();
    }
}
