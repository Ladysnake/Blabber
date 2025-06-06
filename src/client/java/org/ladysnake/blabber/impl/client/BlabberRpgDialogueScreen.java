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

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.blabber.api.client.BlabberDialogueScreen;
import org.ladysnake.blabber.api.layout.DefaultLayoutParams;
import org.ladysnake.blabber.api.layout.Margins;
import org.ladysnake.blabber.impl.client.widgets.DialogueChoiceListWidget;
import org.ladysnake.blabber.impl.client.widgets.RpgDialogueChoiceListWidget;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;

public class BlabberRpgDialogueScreen extends BlabberDialogueScreen<DefaultLayoutParams> {
    public static final int INSTRUCTIONS_BOTTOM_MARGIN = 6;
    public static final int TEXT_TOP_MARGIN = 12;

    public BlabberRpgDialogueScreen(DialogueScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.choiceList.setChoiceColor(0xD0D0D0);
        this.choiceList.setLockedChoiceColor(0xA0A0A0);
        this.choiceList.setSelectedChoiceColor(0xF0F066);
        this.choiceList.setSelectionIconTexture(DIALOGUE_ARROWS.get(1));
        this.choiceList.setLockIconTexture(DIALOGUE_LOCKS.get(2));
    }

    @Override
    protected @NotNull DialogueChoiceListWidget createChoiceList() {
        return new RpgDialogueChoiceListWidget(0, 0, choiceListMaxWidth, 1000, Text.empty(), textRenderer, this::confirmChoice, illustrations);
    }

    @Override
    protected void computeMargins() {
        super.computeMargins();
        Margins mainTextMargins = this.params().getMainTextMargins();
        this.choiceListMaxWidth = 170;
        this.mainTextMaxWidth = Math.min(400, this.width) - mainTextMargins.left() - mainTextMargins.right();
        this.instructionsMinY = this.height - INSTRUCTIONS_BOTTOM_MARGIN - this.textRenderer.getWrappedLinesHeight(instructions, this.width - 5);
        this.mainTextMinY = this.height - 60 - mainTextMargins.bottom();
        this.mainTextMinX = Math.max(mainTextMargins.left(), (this.width / 2) - (Math.min(textRenderer.getWidth(handler.getCurrentText()), mainTextMaxWidth) / 2));
        this.illustrationSlots.get(IllustrationAnchor.BEFORE_MAIN_TEXT).set(
                Math.max(mainTextMargins.left(), (this.width / 2) - (mainTextMaxWidth / 2)),
                this.height - 60
        );
    }

    @Override
    protected void positionChoiceList() {
        Margins mainTextMargins = this.params().getMainTextMargins();
        ((RpgDialogueChoiceListWidget) this.choiceList).setMaxY(mainTextMinY - 25 - mainTextMargins.top());
        this.choiceList.setWidth(choiceListMaxWidth);
        this.choiceList.setX(this.width - this.choiceList.getWidth());
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
    public void renderInGameBackground(DrawContext context) {
        ((RpgDialogueChoiceListWidget) this.choiceList).renderWidgetBackground(context);
        // Bottom background
        context.fillGradient(0, this.mainTextMinY - 20, this.width, this.mainTextMinY - TEXT_TOP_MARGIN, 0x00101010, 0xc0101010);
        context.fillGradient(0, this.mainTextMinY - TEXT_TOP_MARGIN, this.width, this.height, 0xc0101010, 0xd0101010);
    }
}
