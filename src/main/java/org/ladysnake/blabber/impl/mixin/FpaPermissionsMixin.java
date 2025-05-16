/*
 * Blabber
 * Copyright (C) 2022-2025 Ladysnake
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
package org.ladysnake.blabber.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.PermissionLevelSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BooleanSupplier;

// TODO remove once FPA has updated
@Mixin(Permissions.class)
public interface FpaPermissionsMixin {
    @WrapOperation(method = "check(Lnet/minecraft/command/CommandSource;Ljava/lang/String;I)Z", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/util/TriState;orElseGet(Ljava/util/function/BooleanSupplier;)Z"), require = 0)
    private static boolean check(TriState instance, BooleanSupplier supplier, Operation<Boolean> original, CommandSource source, String permission, int defaultRequiredLevel) {
        return original.call(instance, (BooleanSupplier) () -> source instanceof PermissionLevelSource levelSource && levelSource.hasPermissionLevel(defaultRequiredLevel));
    }
}
