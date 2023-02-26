/*
 * Copyright 2023 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
