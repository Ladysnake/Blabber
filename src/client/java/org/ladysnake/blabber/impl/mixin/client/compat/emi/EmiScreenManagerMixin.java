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
package org.ladysnake.blabber.impl.mixin.client.compat.emi;

import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.MinecraftClient;
import org.ladysnake.blabber.api.client.BlabberDialogueScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmiScreenManager.class)
public class EmiScreenManagerMixin {
    @Shadow private static MinecraftClient client;

    @Inject(method = "isDisabled", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void blabber$disableEmiInDialogues(CallbackInfoReturnable<Boolean> cir) {
        if (client.currentScreen instanceof BlabberDialogueScreen) {
            cir.setReturnValue(true);
        }
    }
}
