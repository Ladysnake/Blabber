/*
 * Blabber
 * Copyright (C) 2022-2023 Ladysnake
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
package org.ladysnake.blabber.impl.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;
import org.ladysnake.blabber.api.client.BlabberDialogueScreen;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;
import org.ladysnake.blabber.impl.common.machine.AvailableChoice;

public class BlabberRpgDialogueScreen extends BlabberDialogueScreen {
    public static final int INSTRUCTIONS_BOTTOM_MARGIN = 6;
    protected int choiceListMaxY;

    public BlabberRpgDialogueScreen(DialogueScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.selectionIconTexture = DIALOGUE_ARROWS.get(1);
        this.lockIconTexture = DIALOGUE_LOCKS.get(2);
        this.choiceColor = 0xD0D0D0;
        this.lockedChoiceColor = 0xA0A0A0;
        this.selectedChoiceColor = 0xF0F066;
    }

    @Override
    protected void computeMargins() {
        super.computeMargins();
        this.choiceListMaxWidth = 150;
        this.mainTextMaxWidth = 400;
        this.instructionsMinY = this.height - INSTRUCTIONS_BOTTOM_MARGIN - this.textRenderer.getWrappedLinesHeight(instructions, this.width - 5);
        this.mainTextMinY = this.height - 60;
        this.mainTextMinX = (this.width / 2) - (Math.min(textRenderer.getWidth(handler.getCurrentText()), mainTextMaxWidth) / 2);
        this.choiceListMaxY = mainTextMinY - 25;
        this.choiceListMinY = choiceListMaxY;
        for (AvailableChoice choice : handler.getAvailableChoices()) {
            this.choiceListMinY -= textRenderer.getWrappedLinesHeight(choice.text(), choiceListMaxWidth) + choiceGap;
        }
        this.choiceListMinX = this.width - choiceListMaxWidth;
        this.selectionIconMinX = choiceListMinX - selectionIconSize - 4;
    }

    @Override
    protected boolean shouldSelectChoice(double mouseX, double mouseY, int choiceY, int choiceHeight, int choiceWidth) {
        return mouseX > choiceListMinX - 4 && mouseX <= width && mouseY > choiceY && mouseY < choiceY + choiceHeight;
    }

    @Override
    public void renderBackground(DrawContext context) {
        // Side background
        int y = this.choiceListMinY;
        ImmutableList<AvailableChoice> availableChoices = handler.getAvailableChoices();
        for (int i = 0; i < availableChoices.size(); i++) {
            AvailableChoice choice = availableChoices.get(i);
            int strHeight = this.textRenderer.getWrappedLinesHeight(choice.text(), choiceListMaxWidth);
            fillHorizontalGradient(context, this.choiceListMinX - 2, y, this.width, y + strHeight, 0xc0101010, 0x80101010);
            if (i == selectedChoice) this.selectionIconMarginTop = ((strHeight - 9) / 2) - 4;
            y += strHeight + choiceGap;
        }
        // Bottom background
        context.fillGradient(0, this.mainTextMinY - 20, this.width, this.mainTextMinY - 12, 0x00101010, 0xc0101010);
        context.fillGradient(0, this.mainTextMinY - 12, this.width, this.height, 0xc0101010, 0xd0101010);
    }

    public static void fillHorizontalGradient(DrawContext context, int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
        final int z = 0;
        final int verticalPadding = 2;
        VertexConsumer vertexConsumer = context.getVertexConsumers().getBuffer(RenderLayer.getGui());
        float a0 = (float) ColorHelper.Argb.getAlpha(colorStart) / 255.0F;
        float r0 = (float) ColorHelper.Argb.getRed(colorStart) / 255.0F;
        float g0 = (float) ColorHelper.Argb.getGreen(colorStart) / 255.0F;
        float b0 = (float) ColorHelper.Argb.getBlue(colorStart) / 255.0F;
        float a1 = (float) ColorHelper.Argb.getAlpha(colorEnd) / 255.0F;
        float r1 = (float) ColorHelper.Argb.getRed(colorEnd) / 255.0F;
        float g1 = (float) ColorHelper.Argb.getGreen(colorEnd) / 255.0F;
        float b1 = (float) ColorHelper.Argb.getBlue(colorEnd) / 255.0F;
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        vertexConsumer.vertex(matrix4f, (float)startX, (float)startY - verticalPadding, (float)z).color(r1, g1, b1, a1).next();
        vertexConsumer.vertex(matrix4f, (float)startX, (float)startY, (float)z).color(r0, g0, b0, a0).next();
        vertexConsumer.vertex(matrix4f, (float)endX, (float)startY, (float)z).color(r1, g1, b1, a1).next();
        vertexConsumer.vertex(matrix4f, (float)endX, (float)startY - verticalPadding, (float)z).color(r1, g1, b1, a1).next();

        vertexConsumer.vertex(matrix4f, (float)startX, (float)startY, (float)z).color(r0, g0, b0, a0).next();
        vertexConsumer.vertex(matrix4f, (float)startX, (float)endY, (float)z).color(r0, g0, b0, a0).next();
        vertexConsumer.vertex(matrix4f, (float)endX, (float)endY, (float)z).color(r1, g1, b1, a1).next();
        vertexConsumer.vertex(matrix4f, (float)endX, (float)startY, (float)z).color(r1, g1, b1, a1).next();

        vertexConsumer.vertex(matrix4f, (float)startX, (float)endY, (float)z).color(r0, g0, b0, a0).next();
        vertexConsumer.vertex(matrix4f, (float)startX, (float)endY + verticalPadding, (float)z).color(r1, g1, b1, a1).next();
        vertexConsumer.vertex(matrix4f, (float)endX, (float)endY + verticalPadding, (float)z).color(r1, g1, b1, a1).next();
        vertexConsumer.vertex(matrix4f, (float)endX, (float)endY, (float)z).color(r1, g1, b1, a1).next();
    }
}
