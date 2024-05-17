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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.api.DialogueIllustration;
import org.ladysnake.blabber.impl.common.BlabberDebugComponent;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;
import org.ladysnake.blabber.impl.common.illustrations.PositionTransform;
import org.ladysnake.blabber.impl.common.machine.AvailableChoice;
import org.ladysnake.blabber.impl.common.model.ChoiceResult;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.IntStream;

@ApiStatus.Experimental // half internal, expect some things to change
public class BlabberDialogueScreen extends HandledScreen<DialogueScreenHandler> {
    public static final List<Identifier> DIALOGUE_ARROWS = IntStream.range(1, 6).mapToObj(i -> Blabber.id("container/dialogue/dialogue_arrow_" + i)).toList();
    public static final List<Identifier> DIALOGUE_LOCKS = IntStream.range(1, 4).mapToObj(i -> Blabber.id("container/dialogue/dialogue_lock_" + i)).toList();
    public static final int DEFAULT_TITLE_GAP = 20;
    public static final int DEFAULT_TEXT_MAX_WIDTH = 300;
    public static final int DEFAULT_INSTRUCTIONS_BOTTOM_MARGIN = 30;
    public static final int[] DEBUG_COLORS = new int[] {
            0x42b862,
            0xb84242,
            0xb86a42,
            0x42b87d,
            0x42b8b8,
            0x426ab8,
            0x6a42b8,
            0xb842b8,
    };

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
    protected Vector2i[] illustrationSlots;
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
        this.illustrationSlots = new Vector2i[] { new Vector2i(), new Vector2i() };
    }

    @Override
    protected void init() {
        super.init();
        this.layout();
    }

    protected void layout() {
        this.computeMargins();
        this.layoutIllustrationSlots();
    }

    protected void computeMargins() {
        this.instructionsMinY = this.height - DEFAULT_INSTRUCTIONS_BOTTOM_MARGIN;
        Text text = this.handler.getCurrentText();
        this.choiceListMinY = mainTextMinY + this.textRenderer.getWrappedLinesHeight(text, mainTextMaxWidth) + DEFAULT_TITLE_GAP;
    }

    protected void layoutIllustrationSlots() {
        this.illustrationSlots[0].set(this.width * 3/4, this.choiceListMinY);
        this.illustrationSlots[1].set(this.width * 2/5, this.height * 2/3);
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
                this.layout();
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
        super.render(context, mouseX, mouseY, tickDelta);

        assert client != null;
        assert client.player != null;

        PositionTransform positionTransform = this.createPositionTransform();
        positionTransform.setControlPoints(0, 0, this.width, this.height);

        int y = mainTextMinY;

        for (String illustrationName : this.handler.getCurrentIllustrations()) {
            DialogueIllustration illustration = this.handler.getIllustration(illustrationName);
            if (illustration != null) {
                illustration.render(context, this.textRenderer, positionTransform, mouseX, mouseY, tickDelta);
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

            positionTransform.setControlPoints(choiceListMinX, y, choiceListMinX + choiceListMaxWidth, y + strHeight);

            for (String illustrationName : choice.illustrations()) {
                DialogueIllustration illustration = this.handler.getIllustration(illustrationName);
                if (illustration != null) {
                    illustration.render(context, this.textRenderer, positionTransform, mouseX, mouseY, tickDelta);
                }
            }

            if (selected) {
                if (choice.unavailabilityMessage().isPresent()) {
                    context.drawGuiTexture(lockIconTexture, selectionIconMinX, y + selectionIconMarginTop, selectionIconSize, selectionIconSize);
                    context.drawTooltip(this.textRenderer, choice.unavailabilityMessage().get(), this.hoveringChoice ? mouseX : choiceListMaxWidth, this.hoveringChoice ? mouseY : y);
                } else {
                    context.drawGuiTexture(selectionIconTexture, selectionIconMinX, y + selectionIconMarginTop, selectionIconSize, selectionIconSize);
                }
            }
            y += strHeight + choiceGap;
        }

        context.drawTextWrapped(this.textRenderer, instructions, Math.max((this.width - this.textRenderer.getWidth(instructions)) / 2, 5), instructionsMinY, this.width - 5, 0x808080);

        if (BlabberDebugComponent.get(client.player).isDebugEnabled()) {
            positionTransform.setControlPoints(0, 0, this.width, this.height);
            renderDebugInfo(context, positionTransform, mouseX, mouseY);
        }
    }

    protected @NotNull PositionTransform createPositionTransform() {
        return new PositionTransform(this.illustrationSlots);
    }

    protected void renderDebugInfo(DrawContext context, PositionTransform positionTransform, int mouseX, int mouseY) {
        for (IllustrationAnchor anchor : IllustrationAnchor.values()) {
            int color = DEBUG_COLORS[anchor.ordinal() % DEBUG_COLORS.length];
            context.drawText(this.textRenderer, "x", positionTransform.transformX(anchor, -3), positionTransform.transformY(anchor, -5), color, true);
            MutableText text = Text.empty().append(Text.literal(anchor.asString()).styled(s -> s.withColor(color))).append(" > X: " + positionTransform.inverseTransformX(anchor, mouseX) + ", Y: " + positionTransform.inverseTransformY(anchor, mouseY));
            switch (anchor) {
                case TOP_LEFT, TOP_RIGHT -> {
                    context.drawTooltip(
                            this.textRenderer,
                            text,
                            positionTransform.transformX(anchor, 0),
                            15
                    );
                }

                case CENTER -> context.drawTooltip(
                        this.textRenderer,
                        text,
                        positionTransform.transformX(anchor, 0),
                        positionTransform.transformY(anchor, 0)
                );

                case BOTTOM_LEFT, BOTTOM_RIGHT -> {
                    context.drawTooltip(
                            this.textRenderer,
                            text,
                            positionTransform.transformX(anchor, 0),
                            positionTransform.transformY(anchor, 0)
                    );
                }
                case SLOT_1, SLOT_2 -> {
                    if (anchor.slotId() < this.illustrationSlots.length) {
                        context.drawTooltip(
                                this.textRenderer,
                                text,
                                positionTransform.transformX(anchor, 0),
                                positionTransform.transformY(anchor, 0)
                        );
                    }
                }
            }
        }
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
