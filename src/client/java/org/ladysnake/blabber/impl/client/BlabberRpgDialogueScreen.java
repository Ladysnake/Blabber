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
package org.ladysnake.blabber.impl.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;
import org.ladysnake.blabber.api.client.BlabberDialogueScreen;
import org.ladysnake.blabber.api.layout.DefaultLayoutParams;
import org.ladysnake.blabber.api.layout.Margins;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;
import org.ladysnake.blabber.impl.common.machine.AvailableChoice;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;
import org.ladysnake.blabber.impl.mixin.client.DrawContextAccessor;

public class BlabberRpgDialogueScreen extends BlabberDialogueScreen<DefaultLayoutParams> {
    public static final int INSTRUCTIONS_BOTTOM_MARGIN = 6;
    public static final int TEXT_TOP_MARGIN = 12;
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
        Margins mainTextMargins = this.params().getMainTextMargins();
        this.choiceListMaxWidth = 150;
        this.mainTextMaxWidth = Math.min(400, this.width) - mainTextMargins.left() - mainTextMargins.right();
        this.instructionsMinY = this.height - INSTRUCTIONS_BOTTOM_MARGIN - this.textRenderer.getWrappedLinesHeight(instructions, this.width - 5);
        this.mainTextMinY = this.height - 60 - mainTextMargins.bottom();
        this.mainTextMinX = Math.max(mainTextMargins.left(), (this.width / 2) - (Math.min(textRenderer.getWidth(handler.getCurrentText()), mainTextMaxWidth) / 2));
        this.illustrationSlots.get(IllustrationAnchor.BEFORE_MAIN_TEXT).set(
                Math.max(mainTextMargins.left(), (this.width / 2) - (mainTextMaxWidth / 2)),
                this.height - 60
        );
        this.choiceListMaxY = mainTextMinY - 25 - mainTextMargins.top();
        this.choiceListMinY = choiceListMaxY;
        for (AvailableChoice choice : handler.getAvailableChoices()) {
            this.choiceListMinY -= textRenderer.getWrappedLinesHeight(choice.text(), choiceListMaxWidth) + choiceGap;
        }
        this.choiceListMinX = this.width - choiceListMaxWidth;
        this.selectionIconMinX = choiceListMinX - selectionIconSize - 4;
    }

    @Override
    protected void layoutIllustrationAnchors() {
        // No super call, we redefine every illustration slot either here or in the previous method
        this.illustrationSlots.get(IllustrationAnchor.SPOT_1).set(
                this.width / 4,
                this.mainTextMinY - TEXT_TOP_MARGIN
        );
        this.illustrationSlots.get(IllustrationAnchor.SPOT_2).set(
                (this.choiceListMinX + this.width) / 2,
                this.choiceListMinY * 3/4
        );
    }

    @Override
    protected boolean shouldSelectChoice(double mouseX, double mouseY, int choiceY, int choiceHeight, int choiceWidth) {
        return mouseX > choiceListMinX - 4 && mouseX <= width && mouseY > choiceY && mouseY < choiceY + choiceHeight;
    }

    @Override
    public void renderInGameBackground(DrawContext context) {
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
        context.fillGradient(0, this.mainTextMinY - 20, this.width, this.mainTextMinY - TEXT_TOP_MARGIN, 0x00101010, 0xc0101010);
        context.fillGradient(0, this.mainTextMinY - TEXT_TOP_MARGIN, this.width, this.height, 0xc0101010, 0xd0101010);
    }

    public static void fillHorizontalGradient(DrawContext context, int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
        ((DrawContextAccessor) context).getState().addSimpleElement(
                new HorizontalGradientGuiElementRenderState(
                        RenderPipelines.GUI, new Matrix3x2f(context.getMatrices()), startX, startY, endX, endY, colorStart, colorEnd, null
                )
        );
    }
}
