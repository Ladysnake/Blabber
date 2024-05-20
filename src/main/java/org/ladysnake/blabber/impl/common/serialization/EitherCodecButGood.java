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
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package org.ladysnake.blabber.impl.common.serialization;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

// Taken from https://github.com/Mojang/DataFixerUpper/pull/92
public record EitherCodecButGood<F, S>(Codec<F> first, Codec<S> second) implements Codec<Either<F, S>> {
    public static <T> Codec<T> alternatively(Codec<T> a, Codec<? extends T> b) {
        return new EitherCodecButGood<>(a, b).xmap(either -> either.map(o -> o, o -> o), Either::left);
    }

    @Override
    public <T> DataResult<Pair<Either<F, S>, T>> decode(final DynamicOps<T> ops, final T input) {
        final DataResult<Pair<Either<F, S>, T>> firstRead = first.decode(ops, input).map(vo -> vo.mapFirst(Either::left));
        if (firstRead.result().isPresent()) {
            return firstRead;
        }
        final DataResult<Pair<Either<F, S>, T>> secondRead = second.decode(ops, input).map(vo -> vo.mapFirst(Either::right));
        if (secondRead.result().isPresent()) {
            return secondRead;
        }
        return DataResult.error(() -> "Failed to parse either. First: " + firstRead.error().orElseThrow().message() + "; Second: " + secondRead.error().orElseThrow().message());
    }

    @Override
    public <T> DataResult<T> encode(final Either<F, S> input, final DynamicOps<T> ops, final T prefix) {
        return input.map(
                value1 -> first.encode(value1, ops, prefix),
                value2 -> second.encode(value2, ops, prefix)
        );
    }
}
