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

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.client.illustration.IllustrationContainer;
import org.ladysnake.blabber.impl.common.illustrations.PositionTransform;
import org.ladysnake.blabber.impl.common.machine.AvailableChoice;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;
import org.ladysnake.blabber.impl.common.model.StateType;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.function.IntFunction;

import static org.ladysnake.blabber.api.client.BlabberDialogueScreen.DIALOGUE_ARROWS;
import static org.ladysnake.blabber.api.client.BlabberDialogueScreen.DIALOGUE_LOCKS;

public class DialogueChoiceListWidget extends ScrollableWidget {
    protected final TextRenderer textRenderer;
    private ImmutableList<AvailableChoice> choices = ImmutableList.of();
    private final IntFunction<@Nullable StateType> confirmChoice;
    protected int selectedChoice;
    protected boolean hoveringChoice;
    private int gap = 8;
    /**
     * Vertical offset for the selection/lock icon, based on the individual choice's Y
     */
    protected int selectionIconMarginTop = -4;
    protected int selectionIconSize = 16;
    /**
     * Gap between the selection icon and the text
     */
    protected int selectionIconGap = 5;
    protected int topMargin = 2;
    protected int lockedChoiceColor = 0x808080;
    protected int selectedChoiceColor = 0xE0E044;
    protected int choiceColor = 0xA0A0A0;
    protected Identifier selectionIconTexture = DIALOGUE_ARROWS.getFirst();
    protected Identifier lockIconTexture = DIALOGUE_LOCKS.getFirst();

    private final PositionTransform positionTransform = new PositionTransform(new EnumMap<>(IllustrationAnchor.class));
    protected final IllustrationContainer illustrations;

    public DialogueChoiceListWidget(int x, int y, int width, int height, Text message, TextRenderer textRenderer, IntFunction<@Nullable StateType> confirmChoice, IllustrationContainer illustrations) {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
        this.confirmChoice = confirmChoice;
        this.illustrations = illustrations;
    }

    public ImmutableList<AvailableChoice> getChoices() {
        return choices;
    }

    public void setChoices(ImmutableList<AvailableChoice> choices) {
        this.choices = choices;
    }

    public int getGap() {
        return gap;
    }

    /**
     * Sets the gap between each choice in the list
     */
    public void setGap(int gap) {
        this.gap = gap;
    }

    public int getSelectionIconGap() {
        return selectionIconGap;
    }

    public void setSelectionIconGap(int selectionIconGap) {
        this.selectionIconGap = selectionIconGap;
    }

    public int getChoiceColor() {
        return choiceColor;
    }

    public void setChoiceColor(int choiceColor) {
        this.choiceColor = choiceColor;
    }

    public int getSelectedChoiceColor() {
        return selectedChoiceColor;
    }

    public void setSelectedChoiceColor(int selectedChoiceColor) {
        this.selectedChoiceColor = selectedChoiceColor;
    }

    public int getLockedChoiceColor() {
        return lockedChoiceColor;
    }

    public void setLockedChoiceColor(int lockedChoiceColor) {
        this.lockedChoiceColor = lockedChoiceColor;
    }

    public Identifier getSelectionIconTexture() {
        return selectionIconTexture;
    }

    public void setSelectionIconTexture(Identifier selectionIconTexture) {
        this.selectionIconTexture = selectionIconTexture;
    }

    public Identifier getLockIconTexture() {
        return lockIconTexture;
    }

    public void setLockIconTexture(Identifier lockIconTexture) {
        this.lockIconTexture = lockIconTexture;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        int y = this.getY() + this.topMargin;
        for (int i = 0; i < choices.size(); i++) {
            Text choice = choices.get(i).text();
            int strHeight = this.textRenderer.getWrappedLinesHeight(choice, this.computeTextWidth());
            int strWidth = strHeight == textRenderer.fontHeight ? this.textRenderer.getWidth(choice) : this.computeTextWidth();
            if (this.shouldSelectChoice(mouseX, mouseY, y, strHeight, strWidth)) {
                this.selectedChoice = i;
                this.hoveringChoice = true;
                return;
            }
            y += strHeight + gap;
            this.hoveringChoice = false;
        }
    }

    protected boolean shouldSelectChoice(double mouseX, double mouseY, int choiceY, int choiceHeight, int choiceWidth) {
        return mouseX >= this.getX() && mouseX < (this.getX() + selectionIconSize + selectionIconGap + choiceWidth) && mouseY > choiceY && mouseY < choiceY + choiceHeight;
    }

    private void confirmChoice(int choice) {
        StateType result = this.confirmChoice.apply(choice);
        if (result == null) return;

        switch (result) {
            case DEFAULT -> {
                this.selectedChoice = 0;
                this.hoveringChoice = false;
            }
            case END_DIALOGUE, ASK_CONFIRMATION -> {}
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.visible) {
            return false;
        } else {
            this.scrollDialogueChoice(MathHelper.clamp(verticalAmount, -1.0, 1.0));
            return true;
        }
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        GameOptions options = MinecraftClient.getInstance().options;
        if (key == GLFW.GLFW_KEY_ENTER || options.inventoryKey.matchesKey(key, scancode)) {
            this.confirmChoice(this.selectedChoice);
            return true;
        }
        boolean down = options.backKey.matchesKey(key, scancode);
        if (down || options.forwardKey.matchesKey(key, scancode)) {
            scrollDialogueChoice(down ? -1 : 1);
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    protected void scrollDialogueChoice(double scrollAmount) {
        ImmutableList<AvailableChoice> availableChoices = this.choices;
        if (!availableChoices.isEmpty()) {
            this.selectedChoice = Math.floorMod((int) (this.selectedChoice - scrollAmount), availableChoices.size());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveringChoice) {
            this.confirmChoice(this.selectedChoice);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected int getContentsHeightWithPadding() {
        return computeContentHeight();
    }

    protected int computeContentHeight() {
        int y = 0;
        for (AvailableChoice choice : getChoices()) {
            y += textRenderer.getWrappedLinesHeight(choice.text(), computeTextWidth()) + getGap();
        }
        return y;
    }

    protected int computeTextWidth() {
        return getWidth() - selectionIconSize - selectionIconGap;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 0;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, topMargin + (float)-this.getScrollY());
        renderContents(context, mouseX, mouseY, deltaTicks);
        context.getMatrices().popMatrix();
        context.disableScissor();
        this.drawScrollbar(context);
    }

    protected void renderContents(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int x = this.getX();
        int y = this.getY();

        for (int i = 0; i < choices.size(); i++) {
            AvailableChoice choice = choices.get(i);
            int strHeight = this.textRenderer.getWrappedLinesHeight(choice.text(), computeTextWidth());
            boolean selected = i == this.selectedChoice;
            int choiceColor = choice.unavailabilityMessage().isPresent() ? lockedChoiceColor : selected ? selectedChoiceColor : this.choiceColor;
            context.drawWrappedText(this.textRenderer, choice.text(), x + selectionIconSize + selectionIconGap, y, this.computeTextWidth(), ColorHelper.fullAlpha(choiceColor), false);

            positionTransform.setControlPoints(x, y, x + this.getWidth(), y + strHeight);

            for (String illustrationName : choice.illustrations()) {
                this.illustrations.getRenderer(illustrationName).render(context, this.textRenderer, positionTransform, mouseX, mouseY, deltaTicks);
            }

            if (selected) {
                if (choice.unavailabilityMessage().isPresent()) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, lockIconTexture, x, y + selectionIconMarginTop, selectionIconSize, selectionIconSize);
                    context.drawTooltip(this.textRenderer, choice.unavailabilityMessage().get(), this.hoveringChoice ? mouseX : x, this.hoveringChoice ? mouseY : y);
                } else {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, selectionIconTexture, x, y + selectionIconMarginTop, selectionIconSize, selectionIconSize);
                }
            }
            y += strHeight + getGap();
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.choices.get(this.selectedChoice).text());
    }
}
