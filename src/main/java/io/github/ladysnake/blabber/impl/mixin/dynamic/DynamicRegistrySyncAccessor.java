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
package io.github.ladysnake.blabber.impl.mixin.dynamic;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SerializableRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(SerializableRegistries.class)
public interface DynamicRegistrySyncAccessor {
	@Accessor("REGISTRIES")
	static Map<RegistryKey<? extends Registry<?>>, ?> blabber$getSyncedCodecs() {
		throw new IllegalStateException("Mixin injection failed.");
	}

	@Accessor("REGISTRIES")
	static void blabber$setSyncedCodecs(Map<RegistryKey<? extends Registry<?>>, ?> syncedCodecs) {
		throw new IllegalStateException("Mixin injection failed.");
	}

	@Invoker("add")
	static <E> void blabber$invokeAddSyncedRegistry(ImmutableMap.Builder<RegistryKey<? extends Registry<?>>, ?> builder, RegistryKey<? extends Registry<E>> registryKey, Codec<E> codec) {
		throw new IllegalStateException("Mixin injection failed.");
	}
}
