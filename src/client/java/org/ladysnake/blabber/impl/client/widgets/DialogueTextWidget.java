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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.SingleKeyCache;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

/**
 * A piece of text in a dialogue that can be scrolled through and narrated
 */
public class DialogueTextWidget extends AbstractScrollArea {
    public static final int LINE_HEIGHT = 9;
    private int maxRows;
    private int textMargin = 1;
    private int textColor = CommonColors.WHITE;
    private final SingleKeyCache<CacheKey, MultiLineLabel> typesetter;

    public DialogueTextWidget(int x, int y, int width, int rows, Component message, Font textRenderer) {
        super(x, y, width, rows * LINE_HEIGHT, message);
        this.maxRows = rows;
        this.typesetter = Util.singleKeyCache(
                cacheKey -> MultiLineLabel.create(textRenderer, cacheKey.maxWidth, cacheKey.message)
        );
    }

    public void setTextWidth(int width) {
        this.setWidth(width + SCROLLBAR_WIDTH);
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        this.setScrollAmount(this.scrollAmount());
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public int getTextMargin() {
        return textMargin;
    }

    public void setTextMargin(int textMargin) {
        this.textMargin = textMargin;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        boolean scrollbarDragged = this.updateScrolling(click);
        return super.mouseClicked(click, doubled) || scrollbarDragged;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        boolean up = input.key() == GLFW.GLFW_KEY_UP;
        boolean down = input.key() == GLFW.GLFW_KEY_DOWN;
        if (up || down) {
            double prevScrollY = this.scrollAmount();
            this.setScrollAmount(prevScrollY + (up ? -1 : 1) * this.scrollRate());
            if (prevScrollY != this.scrollAmount()) {
                return true;
            }
        }

        return super.keyPressed(input);
    }

    @Override
    public int contentHeight() {
        return this.getTypesetText().getLineCount() * LINE_HEIGHT;
    }

    @Override
    protected double scrollRate() {
        return LINE_HEIGHT;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        context.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
        context.pose().pushMatrix();
        context.pose().translate(this.textMargin, (float)(this.textMargin - this.scrollAmount()));
        renderContents(context);
        context.pose().popMatrix();
        context.disableScissor();
        this.renderScrollbar(context, mouseX, mouseY);
    }

    private void renderContents(GuiGraphics context) {
        MultiLineLabel multilineText = this.getTypesetText();
        int x = this.getX();
        int y = this.getY();
        multilineText.visitLines(TextAlignment.LEFT, x, y, LINE_HEIGHT, context.textRenderer(
                GuiGraphics.HoveredTextEffects.TOOLTIP_ONLY,
                style -> style.withColor(this.getTextColor())
        ));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, this.getMessage());
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        // NO-OP
    }

    private MultiLineLabel getTypesetText() {
        return this.typesetter.getValue(new CacheKey(this.getMessage(), this.width - this.textMargin - SCROLLBAR_WIDTH, this.maxRows));
    }

    @Environment(EnvType.CLIENT)
    record CacheKey(Component message, int maxWidth, int maxRows) { }
}
