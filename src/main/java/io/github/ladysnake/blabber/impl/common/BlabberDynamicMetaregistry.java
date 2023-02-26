/*
 * Blabber
 * Copyright (C) 2022-2023 Ladysnake
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
package io.github.ladysnake.blabber.impl.common;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.github.ladysnake.blabber.impl.mixin.dynamic.DynamicRegistrySyncAccessor;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;

/**
 * API copied from QuiltMC/quilt-standard-libraries#271 with direct permission from the author (me)
 */
public class BlabberDynamicMetaregistry {
	public static <E> void register(RegistryKey<? extends Registry<E>> ref, Codec<E> entryCodec) {
		RegistryLoader.DYNAMIC_REGISTRIES.add(new RegistryLoader.Entry<>(ref, entryCodec));
	}

	public static <E> void registerSynced(RegistryKey<? extends Registry<E>> ref, Codec<E> entryCodec, Codec<E> syncCodec) {
		register(ref, entryCodec);
		var builder = ImmutableMap.<RegistryKey<? extends Registry<?>>, Object>builder().putAll(DynamicRegistrySyncAccessor.blabber$getSyncedCodecs());
		DynamicRegistrySyncAccessor.blabber$invokeAddSyncedRegistry(builder, ref, syncCodec);
		DynamicRegistrySyncAccessor.blabber$setSyncedCodecs(builder.build());
	}
}
