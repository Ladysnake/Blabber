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
package org.ladysnake.blabber.impl.client.illustrations;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.ladysnake.blabber.api.client.illustration.DialogueIllustrationRenderer;
import org.ladysnake.blabber.impl.common.illustrations.DialogueIllustrationItem;
import org.ladysnake.blabber.impl.common.illustrations.PositionTransform;

public class ItemIllustrationRenderer extends DialogueIllustrationRenderer<DialogueIllustrationItem> {
    public ItemIllustrationRenderer(DialogueIllustrationItem illustration) {
        super(illustration);
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, PositionTransform positionTransform, int mouseX, int mouseY, float tickDelta) {
        // We draw the actual item, then the count and bar and such.
        try {
            ItemStack stack = this.illustration.stack();
            float scale = this.illustration.scale();

            ((DrawContextHooks) context).blabber$setItemScale(scale);
            int originX = positionTransform.transformX(this.illustration.anchor(), this.illustration.x());
            int originY = positionTransform.transformY(this.illustration.anchor(), this.illustration.y());
            context.drawItem(stack, originX + Math.round(8 * (scale - 1)), originY + Math.round(8 * (scale - 1)));
            if (scale == 1) {  // Not supporting rescaled stack decorations right now
                context.drawItemInSlot(textRenderer, stack, originX, originY);
            }
            if (this.illustration.showTooltip() &&
                    originX <= mouseX && originX + (16 * scale) + 4 > mouseX &&
                    originY <= mouseY && originY + (16 * scale) + 4 > mouseY) {
                context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
            }
        } finally {
            ((DrawContextHooks) context).blabber$setItemScale(1f);
        }
    }

    public interface DrawContextHooks {
        void blabber$setItemScale(float itemScale);
    }
}
