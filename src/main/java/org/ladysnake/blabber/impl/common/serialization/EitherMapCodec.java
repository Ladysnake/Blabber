// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package org.ladysnake.blabber.impl.common.serialization;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.EitherCodec;

import java.util.stream.Stream;

/**
 * Adapts the decoding fix from {@link EitherCodec} (which is missing in {@link com.mojang.serialization.codecs.EitherMapCodec})
 */
public class EitherMapCodec<F, S> extends MapCodec<Either<F, S>> {
    private final MapCodec<F> first;
    private final MapCodec<S> second;

    public EitherMapCodec(MapCodec<F> first, MapCodec<S> second) {
        this.first = first;
        this.second = second;
    }

    public static <T> MapCodec<T> alternatively(MapCodec<T> a, MapCodec<? extends T> b) {
        return new EitherMapCodec<>(a, b).xmap(either -> either.map(o -> o, o -> o), Either::left);
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.empty();
    }

    @Override
    public <T> DataResult<Either<F, S>> decode(DynamicOps<T> ops, MapLike<T> input) {
        final DataResult<Either<F, S>> firstRead = first.decode(ops, input).map(Either::left);
        if (firstRead.result().isPresent()) {
            return firstRead;
        }
        final DataResult<Either<F, S>> secondRead = second.decode(ops, input).map(Either::right);
        if (secondRead.result().isPresent()) {
            return secondRead;
        }
        return DataResult.error(() -> "Failed to parse either. First: " + firstRead.error().orElseThrow().message() + "; Second: " + secondRead.error().orElseThrow().message());
    }

    @Override
    public <T> RecordBuilder<T> encode(Either<F, S> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        return input.map(
                value1 -> first.encode(value1, ops, prefix),
                value2 -> second.encode(value2, ops, prefix)
        );
    }
}