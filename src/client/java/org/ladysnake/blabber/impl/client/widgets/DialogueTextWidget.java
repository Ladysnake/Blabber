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
package org.ladysnake.blabber.impl.client.widgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.CachedMapper;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import org.lwjgl.glfw.GLFW;

/**
 * A piece of text in a dialogue that can be scrolled through and narrated
 */
public class DialogueTextWidget extends ScrollableWidget {
    public static final int LINE_HEIGHT = 9;
    private int maxRows;
    private int textMargin = 1;
    private int textColor = Colors.WHITE;
    private final CachedMapper<CacheKey, MultilineText> typesetter;

    public DialogueTextWidget(int x, int y, int width, int rows, Text message, TextRenderer textRenderer) {
        super(x, y, width, rows * LINE_HEIGHT, message);
        this.maxRows = rows;
        this.typesetter = Util.cachedMapper(
                cacheKey -> MultilineText.create(textRenderer, cacheKey.maxWidth, cacheKey.message)
        );
    }

    public void setTextWidth(int width) {
        this.setWidth(width + SCROLLBAR_WIDTH);
    }

    @Override
    public void setMessage(Text message) {
        super.setMessage(message);
        this.setScrollY(this.getScrollY());
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean scrollbarDragged = this.checkScrollbarDragged(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button) || scrollbarDragged;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean up = keyCode == GLFW.GLFW_KEY_UP;
        boolean down = keyCode == GLFW.GLFW_KEY_DOWN;
        if (up || down) {
            double prevScrollY = this.getScrollY();
            this.setScrollY(prevScrollY + (up ? -1 : 1) * this.getDeltaYPerScroll());
            if (prevScrollY != this.getScrollY()) {
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public int getContentsHeightWithPadding() {
        return this.getTypesetText().count() * LINE_HEIGHT;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return LINE_HEIGHT;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(this.textMargin, (float)(this.textMargin - this.getScrollY()));
        renderContents(context);
        context.getMatrices().popMatrix();
        context.disableScissor();
        this.drawScrollbar(context);
    }

    private void renderContents(DrawContext context) {
        MultilineText multilineText = this.getTypesetText();
        int x = this.getX();
        int y = this.getY();
        int l = ColorHelper.withAlpha(this.alpha, this.getTextColor());
        multilineText.draw(context, x, y, LINE_HEIGHT, l);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.getMessage());
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        // NO-OP
    }

    private MultilineText getTypesetText() {
        return this.typesetter.map(new CacheKey(this.getMessage(), this.width - this.textMargin - SCROLLBAR_WIDTH, this.maxRows));
    }

    @Environment(EnvType.CLIENT)
    record CacheKey(Text message, int maxWidth, int maxRows) { }
}
