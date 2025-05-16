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
package org.ladysnake.blabber.impl.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.render.GuiRenderer;
import org.ladysnake.blabber.impl.client.illustrations.ItemIllustrationRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @ModifyExpressionValue(
            method = "prepareItem",
            at = @At(value = "CONSTANT", args = "intValue=16")
    )
    private int scaleItem(int original, @Coerce ItemIllustrationRenderer.RenderStateHooks renderState) {
        return Math.round(original * renderState.blabber$getItemScale());
    }
}
