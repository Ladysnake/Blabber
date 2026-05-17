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
package org.ladysnake.blabber.impl.client.widgets;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.ladysnake.blabber.api.client.illustration.IllustrationContainer;
import org.ladysnake.blabber.impl.client.HorizontalGradientGuiElementRenderState;
import org.ladysnake.blabber.impl.common.machine.AvailableChoice;
import org.ladysnake.blabber.impl.common.model.StateType;
import org.ladysnake.blabber.impl.mixin.client.GuiGraphicsExtractorAccessor;

import java.util.function.IntFunction;

public class RpgDialogueChoiceListWidget extends DialogueChoiceListWidget {
    public RpgDialogueChoiceListWidget(int x, int y, int width, int height, Component message, Font textRenderer, IntFunction<@Nullable StateType> confirmChoice, IllustrationContainer illustrations) {
        super(x, y, width, height, message, textRenderer, confirmChoice, illustrations);
    }

    public void setMaxY(int maxY) {
        this.setY(maxY - computeContentHeight());
    }

    @Override
    protected boolean shouldSelectChoice(double mouseX, double mouseY, int choiceY, int choiceHeight, int choiceWidth) {
        return mouseX >= this.getX() && mouseX < (this.getX() + this.getWidth()) && mouseY > choiceY && mouseY < choiceY + choiceHeight;
    }

    public void renderWidgetBackground(GuiGraphicsExtractor context) {
        int y = this.getY() + this.topMargin / 2;
        ImmutableList<AvailableChoice> availableChoices = this.getChoices();
        for (int i = 0; i < availableChoices.size(); i++) {
            AvailableChoice choice = availableChoices.get(i);
            int strHeight = this.textRenderer.wordWrapHeight(choice.text(), this.computeTextWidth());
            fillHorizontalGradient(context, this.getX() + this.selectionIconSize + this.selectionIconGap - 2, y, this.getX() + this.width, y + strHeight, 0xc0101010, 0x80101010);
            if (i == selectedChoice) this.selectionIconMarginTop = ((strHeight - textRenderer.lineHeight) / 2) - 4;
            y += strHeight + getGap();
        }
    }

    public static void fillHorizontalGradient(GuiGraphicsExtractor context, int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
        ((GuiGraphicsExtractorAccessor) context).getGuiRenderState().addGuiElement(
                new HorizontalGradientGuiElementRenderState(
                        RenderPipelines.GUI, new Matrix3x2f(context.pose()), startX, startY, endX, endY, colorStart, colorEnd, null
                )
        );
    }
}
