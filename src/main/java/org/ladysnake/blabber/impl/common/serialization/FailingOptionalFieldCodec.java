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
package org.ladysnake.blabber.impl.common.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.OptionalFieldCodec;

import java.util.Objects;
import java.util.Optional;

/** Just like {@link com.mojang.serialization.codecs.OptionalFieldCodec}, except it errors if you got invalid input */
public class FailingOptionalFieldCodec<A> extends OptionalFieldCodec<A> {
    public static <A> MapCodec<Optional<A>> of(Codec<A> elementCodec, String name) {
        return new FailingOptionalFieldCodec<>(name, elementCodec);
    }

    public static <A> MapCodec<A> of(Codec<A> elementCodec, String name, A defaultValue) {
        return of(elementCodec, name).xmap(
                o -> o.orElse(defaultValue),
                a -> Objects.equals(a, defaultValue) ? Optional.empty() : Optional.of(a)
        );
    }

    private final String name;
    private final Codec<A> elementCodec;

    private FailingOptionalFieldCodec(String name, Codec<A> elementCodec) {
        super(name, elementCodec);
        this.name = name;
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> DataResult<Optional<A>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final T value = input.get(name);
        if (value == null) {
            return DataResult.success(Optional.empty());
        }

        return elementCodec.parse(ops, value).map(Optional::ofNullable);
    }
}
