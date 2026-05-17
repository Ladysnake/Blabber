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

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.impl.client.illustrations.ItemIllustrationRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiItemRenderState.class)
public class GuiItemRenderStateMixin implements ItemIllustrationRenderer.RenderStateHooks {
    @Shadow @Final @Mutable
    private @Nullable ScreenRectangle bounds;
    @Unique
    private float blabber$itemScale = 1;

    @Override
    public void blabber$setItemScale(float itemScale) {
        this.blabber$itemScale = itemScale;
        if (this.bounds != null) {
            this.bounds = new ScreenRectangle(this.bounds.position(), Math.round(this.bounds.width() * itemScale), Math.round(this.bounds.height() * itemScale));
        }
    }

    @Override
    public float blabber$getItemScale() {
        return blabber$itemScale;
    }
}
