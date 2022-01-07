/*
 * Blabber
 * Copyright (C) 2022 Ladysnake
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
package io.github.ladysnake.blabber.impl.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.github.ladysnake.blabber.impl.common.BlabberRegistrar;
import io.github.ladysnake.blabber.impl.common.DialogueTemplate;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DynamicRegistryManager.class)
public abstract class DynamicRegistryManagerMixin {
    @Shadow
    private static void register(ImmutableMap.Builder<RegistryKey<? extends Registry<?>>, ?> infosBuilder, RegistryKey<? extends Registry<?>> registryRef, Codec<?> entryCodec, Codec<?> networkEntryCodec) {}

    @Dynamic("Lambda for INFOS initialization through Util#make")
    @Inject(method = "method_30531", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void buildDynamicRegistries(CallbackInfoReturnable<ImmutableMap<RegistryKey<? extends Registry<?>>, ?>> cir, ImmutableMap.Builder<RegistryKey<? extends Registry<?>>, ?> builder) {
        register(builder, BlabberRegistrar.DIALOGUE_REGISTRY_KEY, DialogueTemplate.CODEC, DialogueTemplate.NETWORK_CODEC);
    }
}
