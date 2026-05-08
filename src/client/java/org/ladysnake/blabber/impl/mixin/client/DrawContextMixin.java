/*
 * Blabber
 * Copyright (C) 2022-2026 Ladysnake
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

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.ItemGuiElementRenderState;
import org.ladysnake.blabber.impl.client.illustrations.ItemIllustrationRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin implements ItemIllustrationRenderer.DrawContextHooks {
    @Unique
    private float blabber$itemScale = 1;

    @Override
    public void blabber$setItemScale(float itemScale) {
        this.blabber$itemScale = itemScale;
    }

    @ModifyArg(
            method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;III)V",
            at = @At(value = "INVOKE", target = "net/minecraft/client/gui/render/state/GuiRenderState.addItem(Lnet/minecraft/client/gui/render/state/ItemGuiElementRenderState;)V")
    )
    private ItemGuiElementRenderState scaleItem(ItemGuiElementRenderState state) {
        ((ItemIllustrationRenderer.RenderStateHooks)(Object) state).blabber$setItemScale(blabber$itemScale);
        return state;
    }
}
