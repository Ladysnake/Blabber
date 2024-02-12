/*
 * Blabber
 * Copyright (C) 2022-2024 Ladysnake
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
package org.ladysnake.blabber.api.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.api.DialogueIllustration;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;
import org.ladysnake.blabber.impl.common.machine.AvailableChoice;
import org.ladysnake.blabber.impl.common.model.ChoiceResult;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.IntStream;

@ApiStatus.Experimental // half internal, expect some things to change
public class BlabberDialogueScreen extends HandledScreen<DialogueScreenHandler> {
    public static final List<Identifier> DIALOGUE_ARROWS = IntStream.range(1, 6).mapToObj(i -> Blabber.id("textures/gui/sprites/container/dialogue/dialogue_arrow_" + i + ".png")).toList();
    public static final List<Identifier> DIALOGUE_LOCKS = IntStream.range(1, 4).mapToObj(i -> Blabber.id("textures/gui/sprites/container/dialogue/dialogue_lock_" + i + ".png")).toList();
    public static final int DEFAULT_TITLE_GAP = 20;
    public static final int DEFAULT_TEXT_MAX_WIDTH = 300;
    public static final int DEFAULT_INSTRUCTIONS_BOTTOM_MARGIN = 30;

    protected final Text instructions;

    // Things that could be constants but may be mutated by subclasses
    protected Identifier selectionIconTexture = DIALOGUE_ARROWS.get(0);
    protected Identifier lockIconTexture = DIALOGUE_LOCKS.get(0);
    /**
     * Margin from the top of the screen to the dialogue's main text
     */
    protected int mainTextMinY = 40;
    /**
     * Gap between each choice in the list
     */
    protected int choiceGap = 8;
    protected int mainTextMinX = 10;
    protected int instructionsMinY;
    protected int mainTextMaxWidth = DEFAULT_TEXT_MAX_WIDTH;
    /**
     * Max width for the choice texts
     */
    protected int choiceListMaxWidth = DEFAULT_TEXT_MAX_WIDTH;
    /**
     * Margin from the left of the screen to the choice list (includes the space for the selection icon)
     */
    protected int choiceListMinX = 25;
    /**
     * Margin from the left of the screen to the choice selection icon
     */
    protected int selectionIconMinX = 4;
    /**
     * Vertical offset for the selection/lock icon, based on the individual choice's Y
     */
    protected int selectionIconMarginTop = -4;
    protected int selectionIconSize = 16;
    protected int mainTextColor = 0xFFFFFF;
    protected int lockedChoiceColor = 0x808080;
    protected int selectedChoiceColor = 0xE0E044;
    protected int choiceColor = 0xA0A0A0;

    // Things that are mutated during state changes
    protected int choiceListMinY;

    // Screen state
    protected int selectedChoice;
    protected boolean hoveringChoice;

    public BlabberDialogueScreen(DialogueScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        GameOptions options = MinecraftClient.getInstance().options;
        this.instructions = Text.translatable("blabber:dialogue.instructions", options.forwardKey.getBoundKeyLocalizedText(), options.backKey.getBoundKeyLocalizedText(), options.inventoryKey.getBoundKeyLocalizedText());
    }

    @Override
    protected void init() {
        super.init();
        this.computeMargins();
    }

    protected void computeMargins() {
        this.instructionsMinY = this.height - DEFAULT_INSTRUCTIONS_BOTTOM_MARGIN;
        Text text = this.handler.getCurrentText();
        this.choiceListMinY = mainTextMinY + this.textRenderer.getWrappedLinesHeight(text, mainTextMaxWidth) + DEFAULT_TITLE_GAP;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !this.handler.isUnskippable();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (hoveringChoice) {
            this.confirmChoice(this.selectedChoice);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        GameOptions options = MinecraftClient.getInstance().options;
        if (key == GLFW.GLFW_KEY_ENTER || options.inventoryKey.matchesKey(key, scancode)) {
            this.confirmChoice(this.selectedChoice);
            return true;
        }
        boolean tab = GLFW.GLFW_KEY_TAB == key;
        boolean down = options.backKey.matchesKey(key, scancode);
        boolean shift = (GLFW.GLFW_MOD_SHIFT & modifiers) != 0;
        if (tab || down || options.forwardKey.matchesKey(key, scancode)) {
            scrollDialogueChoice(tab && !shift || down ? -1 : 1);
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    private @Nullable ChoiceResult confirmChoice(int selectedChoice) {
        assert this.client != null;
        if (this.handler.getAvailableChoices().get(selectedChoice).unavailabilityMessage().isPresent()) {
            return null;
        }

        ChoiceResult result = this.handler.makeChoice(selectedChoice);

        switch (result) {
            case END_DIALOGUE -> this.client.setScreen(null);
            case ASK_CONFIRMATION -> {
                ImmutableList<AvailableChoice> choices = this.handler.getAvailableChoices();
                this.client.setScreen(new ConfirmScreen(
                    this::onBigChoiceMade,
                    this.handler.getCurrentText(),
                    Text.empty(),
                    choices.get(0).text(),
                    choices.get(1).text()
                ));
            }
            default -> {
                this.selectedChoice = 0;
                this.hoveringChoice = false;
                this.computeMargins();
            }
        }

        return result;
    }

    private void onBigChoiceMade(boolean yes) {
        assert client != null;
        if (this.confirmChoice(yes ? 0 : 1) == ChoiceResult.DEFAULT) {
            this.client.setScreen(this);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        this.scrollDialogueChoice(MathHelper.clamp(verticalAmount, -1.0, 1.0));
        return true;
    }

    protected void scrollDialogueChoice(double scrollAmount) {
        ImmutableList<AvailableChoice> availableChoices = this.handler.getAvailableChoices();
        if (!availableChoices.isEmpty()) {
            this.selectedChoice = Math.floorMod((int) (this.selectedChoice - scrollAmount), availableChoices.size());
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        ImmutableList<AvailableChoice> choices = this.handler.getAvailableChoices();
        int y = this.choiceListMinY;
        for (int i = 0; i < choices.size(); i++) {
            Text choice = choices.get(i).text();
            int strHeight = this.textRenderer.getWrappedLinesHeight(choice, choiceListMaxWidth);
            int strWidth = strHeight == 9 ? this.textRenderer.getWidth(choice) : choiceListMaxWidth;
            if (this.shouldSelectChoice(mouseX, mouseY, y, strHeight, strWidth)) {
                this.selectedChoice = i;
                this.hoveringChoice = true;
                return;
            }
            y += strHeight + choiceGap;
            this.hoveringChoice = false;
        }
    }

    protected boolean shouldSelectChoice(double mouseX, double mouseY, int choiceY, int choiceHeight, int choiceWidth) {
        return mouseX >= 0 && mouseX < (choiceListMinX + choiceWidth) && mouseY > choiceY && mouseY < choiceY + choiceHeight;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        this.renderBackground(context);

        assert client != null;

        int y = mainTextMinY;

        for (String illustrationName : this.handler.getCurrentIllustrations()) {
            DialogueIllustration illustration = this.handler.getIllustration(illustrationName);
            if (illustration != null) {
                illustration.render(context, this.textRenderer, 0, 0, mouseX, mouseY, tickDelta);
            }
        }

        Text mainText = this.handler.getCurrentText();

        context.drawTextWrapped(this.textRenderer, mainText, mainTextMinX, y, mainTextMaxWidth, mainTextColor);
        y = this.choiceListMinY;
        ImmutableList<AvailableChoice> choices = this.handler.getAvailableChoices();

        for (int i = 0; i < choices.size(); i++) {
            AvailableChoice choice = choices.get(i);
            int strHeight = this.textRenderer.getWrappedLinesHeight(choice.text(), choiceListMaxWidth);
            boolean selected = i == this.selectedChoice;
            int choiceColor = choice.unavailabilityMessage().isPresent() ? lockedChoiceColor : selected ? selectedChoiceColor : this.choiceColor;
            context.drawTextWrapped(this.textRenderer, choice.text(), choiceListMinX, y, choiceListMaxWidth, choiceColor);

            for (String illustrationName : choice.illustrations()) {
                DialogueIllustration illustration = this.handler.getIllustration(illustrationName);
                if (illustration != null) {
                    illustration.render(context, this.textRenderer, choiceListMinX, y, mouseX, mouseY, tickDelta);
                }
            }

            if (selected) {
                if (choice.unavailabilityMessage().isPresent()) {
                    context.drawTexture(
                        lockIconTexture,
                        selectionIconMinX,
                        y + selectionIconMarginTop,
                        0,
                        0,
                        selectionIconSize,
                        selectionIconSize
                    );
                    context.drawTooltip(
                        this.textRenderer,
                        choice.unavailabilityMessage().get(),
                        this.hoveringChoice ? mouseX : choiceListMaxWidth,
                        this.hoveringChoice ? mouseY : y
                    );
                } else {
                    context.drawTexture(
                        selectionIconTexture,
                        selectionIconMinX,
                        y + selectionIconMarginTop,
                        0,
                        0,
                        selectionIconSize,
                        selectionIconSize
                    );
                }
            }
            y += strHeight + choiceGap;
        }

        context.drawTextWrapped(this.textRenderer, instructions, Math.max((this.width - this.textRenderer.getWidth(instructions)) / 2, 5), instructionsMinY, this.width - 5, 0x808080);

        super.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    protected void drawBackground(DrawContext matrices, float delta, int mouseX, int mouseY) {
        // NO-OP
    }

    @Override
    protected void drawForeground(DrawContext matrices, int mouseX, int mouseY) {
        // NO-OP
    }
}
