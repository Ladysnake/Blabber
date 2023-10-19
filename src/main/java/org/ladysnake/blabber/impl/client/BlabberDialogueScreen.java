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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;
import org.ladysnake.blabber.impl.common.machine.AvailableChoice;
import org.ladysnake.blabber.impl.common.model.ChoiceResult;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.IntStream;

public class BlabberDialogueScreen extends HandledScreen<DialogueScreenHandler> {
    public static final List<Identifier> DIALOGUE_ARROWS = IntStream.range(1, 6).mapToObj(i -> Blabber.id("container/dialogue/dialogue_arrow_" + i)).toList();
    public static final List<Identifier> DIALOGUE_LOCKS = IntStream.range(1, 4).mapToObj(i -> Blabber.id("container/dialogue/dialogue_lock_" + i)).toList();

    public static final int MIN_RENDER_Y = 40;
    public static final int TITLE_GAP = 20;
    public static final int CHOICE_GAP = 8;
    public static final int MAX_TEXT_WIDTH = 300;
    public static final int CHOICE_LEFT_MARGIN = 25;
    public static final int LOCKED_CHOICE_COLOR = 0x808080;
    public static final int SELECTED_CHOICE_COLOR = 0xE0E044;
    public static final int CHOICE_COLOR = 0xA0A0A0;

    private int selectedChoice;
    private boolean hoveringChoice;
    private final Text instructions;

    public BlabberDialogueScreen(DialogueScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        GameOptions options = MinecraftClient.getInstance().options;
        this.instructions = Text.translatable("blabber:dialogue.instructions", options.forwardKey.getBoundKeyLocalizedText(), options.backKey.getBoundKeyLocalizedText(), options.inventoryKey.getBoundKeyLocalizedText());
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollDialogueChoice(verticalAmount);
        return true;
    }

    private void scrollDialogueChoice(double scrollAmount) {
        ImmutableList<AvailableChoice> availableChoices = this.handler.getAvailableChoices();
        if (!availableChoices.isEmpty()) {
            this.selectedChoice = Math.floorMod((int) (this.selectedChoice - scrollAmount), availableChoices.size());
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        ImmutableList<AvailableChoice> choices = this.handler.getAvailableChoices();
        Text title = this.handler.getCurrentText();
        int y = MIN_RENDER_Y + this.getTextBoundedHeight(title, MAX_TEXT_WIDTH) + TITLE_GAP;
        for (int i = 0; i < choices.size(); i++) {
            Text choice = choices.get(i).text();
            int strHeight = this.getTextBoundedHeight(choice, width);
            int strWidth = strHeight == 9 ? this.textRenderer.getWidth(choice) : width;
            if (mouseX < (CHOICE_LEFT_MARGIN + strWidth) && mouseY > y && mouseY < y + strHeight) {
                this.selectedChoice = i;
                this.hoveringChoice = true;
                return;
            }
            y += strHeight + CHOICE_GAP;
            this.hoveringChoice = false;
        }
    }

    private int getTextBoundedHeight(Text text, int maxWidth) {
        return 9 * this.textRenderer.wrapLines(text, maxWidth).size();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        super.render(context, mouseX, mouseY, tickDelta);

        assert client != null;

        int y = MIN_RENDER_Y;
        Text title = this.handler.getCurrentText();

        context.drawTextWrapped(this.textRenderer, title, 10, y, MAX_TEXT_WIDTH, 0xFFFFFF);
        y += this.getTextBoundedHeight(title, MAX_TEXT_WIDTH) + TITLE_GAP;
        ImmutableList<AvailableChoice> choices = this.handler.getAvailableChoices();

        for (int i = 0; i < choices.size(); i++) {
            AvailableChoice choice = choices.get(i);
            int strHeight = this.getTextBoundedHeight(choice.text(), MAX_TEXT_WIDTH);
            boolean selected = i == this.selectedChoice;
            int choiceColor = choice.unavailabilityMessage().isPresent() ? LOCKED_CHOICE_COLOR : selected ? SELECTED_CHOICE_COLOR : CHOICE_COLOR;
            context.drawTextWrapped(this.textRenderer, choice.text(), CHOICE_LEFT_MARGIN, y, MAX_TEXT_WIDTH, choiceColor);
            if (selected) {
                int choiceIconSize = 16;
                if (choice.unavailabilityMessage().isPresent()) {
                    context.drawGuiTexture(DIALOGUE_LOCKS.get(0), 4, y - 4, choiceIconSize, choiceIconSize);
                    context.drawTooltip(this.textRenderer, choice.unavailabilityMessage().get(), this.hoveringChoice ? mouseX : MAX_TEXT_WIDTH, this.hoveringChoice ? mouseY : y);
                } else {
                    context.drawGuiTexture(DIALOGUE_ARROWS.get(0), 4, y - 4, choiceIconSize, choiceIconSize);
                }
            }
            y += strHeight + CHOICE_GAP;
        }

        context.drawText(this.textRenderer, instructions, (this.width - this.textRenderer.getWidth(instructions)) / 2, this.height - 30, 0x808080, false);
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
