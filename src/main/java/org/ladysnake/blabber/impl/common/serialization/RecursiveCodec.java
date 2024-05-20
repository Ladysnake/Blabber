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

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.function.Function;
import java.util.function.Supplier;

/** Literally stolen from DFU 7.0 */
public class RecursiveCodec<T> implements Codec<T> {
    public static <A> Codec<A> of(String name, Function<Codec<A>, Codec<A>> wrapped) {
        return new RecursiveCodec<>(name, wrapped);
    }

    private final String name;
    private final Supplier<Codec<T>> wrapped;

    private RecursiveCodec(final String name, final Function<Codec<T>, Codec<T>> wrapped) {
        this.name = name;
        this.wrapped = Suppliers.memoize(() -> wrapped.apply(this));
    }

    @Override
    public <S> DataResult<Pair<T, S>> decode(final DynamicOps<S> ops, final S input) {
        return wrapped.get().decode(ops, input);
    }

    @Override
    public <S> DataResult<S> encode(final T input, final DynamicOps<S> ops, final S prefix) {
        return wrapped.get().encode(input, ops, prefix);
    }

    @Override
    public String toString() {
        return "RecursiveCodec[" + name + ']';
    }
}

