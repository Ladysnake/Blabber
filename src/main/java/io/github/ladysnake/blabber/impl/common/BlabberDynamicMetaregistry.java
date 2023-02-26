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
