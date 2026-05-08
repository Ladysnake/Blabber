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
package org.ladysnake.blabber.api.client;

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.api.client.illustration.IllustrationContainer;
import org.ladysnake.blabber.api.layout.DialogueLayout;
import org.ladysnake.blabber.impl.client.widgets.DialogueChoiceListWidget;
import org.ladysnake.blabber.impl.client.widgets.DialogueTextWidget;
import org.ladysnake.blabber.impl.common.DialogueScreenHandler;
import org.ladysnake.blabber.impl.common.illustrations.PositionTransform;
import org.ladysnake.blabber.impl.common.machine.AvailableChoice;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;
import org.ladysnake.blabber.impl.common.model.StateType;
import org.ladysnake.blabber.impl.common.packets.ChoiceSelectionPayload;
import org.ladysnake.blabber.impl.common.settings.BlabberSetting;
import org.ladysnake.blabber.impl.common.settings.BlabberSettingsComponent;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.IntStream;

@ApiStatus.Experimental // half internal, expect some things to change
public class BlabberDialogueScreen<P extends DialogueLayout.Params> extends AbstractContainerScreen<DialogueScreenHandler> {
    public static final List<Identifier> DIALOGUE_ARROWS = IntStream.range(1, 6).mapToObj(i -> Blabber.id("container/dialogue/dialogue_arrow_" + i)).toList();
    public static final List<Identifier> DIALOGUE_LOCKS = IntStream.range(1, 4).mapToObj(i -> Blabber.id("container/dialogue/dialogue_lock_" + i)).toList();
    public static final int DEFAULT_TITLE_GAP = 20;
    public static final int DEFAULT_TEXT_MAX_WIDTH = 320;
    public static final int DEFAULT_TEXT_MAX_ROWS = 8;
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

    protected final Component instructions;

    // Things that could be constants but may be mutated by subclasses
    /**
     * Margin from the top of the screen to the dialogue's main text
     */
    protected int mainTextMinY = 40;
    protected int mainTextMinX = 8;
    protected int instructionsMinY;
    protected int mainTextMaxWidth = DEFAULT_TEXT_MAX_WIDTH;
    protected int mainTextMaxRows = DEFAULT_TEXT_MAX_ROWS;
    /**
     * Max width for the choice texts
     */
    protected int choiceListMaxWidth = DEFAULT_TEXT_MAX_WIDTH;
    /**
     * Margin from the left of the screen to the choice list
     */
    protected int choiceListMinX = 4;
    protected EnumMap<IllustrationAnchor, Vector2i> illustrationSlots;
    protected int mainTextColor = 0xFFFFFF;

    // Widgets
    protected DialogueTextWidget title;
    protected DialogueChoiceListWidget choiceList;

    protected final IllustrationContainer illustrations = new IllustrationContainer();

    public BlabberDialogueScreen(DialogueScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        Options options = Minecraft.getInstance().options;
        this.instructions = Component.translatable("blabber:dialogue.instructions", options.keyUp.getTranslatedKeyMessage(), options.keyDown.getTranslatedKeyMessage(), options.keyInventory.getTranslatedKeyMessage());
        this.illustrationSlots = new EnumMap<>(IllustrationAnchor.class);
        for (IllustrationAnchor anchor : IllustrationAnchor.values()) {
            this.illustrationSlots.put(anchor, new Vector2i(-999, -999));
        }
    }

    @SuppressWarnings("unchecked")
    protected P params() {
        return (P) this.menu.getLayout().params();
    }

    @Override
    protected void init() {
        super.init();
        this.title = this.addRenderableWidget(new DialogueTextWidget(0, 0, mainTextMaxWidth, mainTextMaxRows, Component.empty(), this.font));
        this.choiceList = this.addRenderableWidget(createChoiceList());
        this.prepareLayout();
        this.illustrations.setIllustrations(this.menu.getIllustrations());
    }

    protected @NotNull DialogueChoiceListWidget createChoiceList() {
        return new DialogueChoiceListWidget(0, 0, choiceListMaxWidth, 1000, Component.empty(), font, this::confirmChoice, illustrations);
    }

    protected void prepareLayout() {
        this.computeMargins();
        this.title.setPosition(mainTextMinX, mainTextMinY);
        this.title.setTextWidth(mainTextMaxWidth);
        this.title.setTextColor(mainTextColor);
        this.title.setMessage(menu.getCurrentText());
        this.choiceList.setChoices(menu.getAvailableChoices());
        this.positionChoiceList();
        this.layoutIllustrationAnchors();
    }

    protected void positionChoiceList() {
        this.choiceList.setPosition(choiceListMinX, this.title.getY() + Math.min(this.title.contentHeight(), this.title.getHeight()) + DEFAULT_TITLE_GAP);
    }

    protected void computeMargins() {
        this.instructionsMinY = this.height - DEFAULT_INSTRUCTIONS_BOTTOM_MARGIN;
    }

    protected void layoutIllustrationAnchors() {
        this.illustrationSlots.get(IllustrationAnchor.BEFORE_MAIN_TEXT).set(this.title.getX(), this.title.getY());
        this.illustrationSlots.get(IllustrationAnchor.SPOT_1).set(this.width * 3/4, this.choiceList.getY());
        this.illustrationSlots.get(IllustrationAnchor.SPOT_2).set(this.width * 2/5, this.height * 2/3);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        Options options = Minecraft.getInstance().options;
        if (options.keyUp.matches(input) || options.keyDown.matches(input) || options.keyInventory.matches(input)) {
            this.setFocused(this.choiceList);
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !this.menu.isUnskippable();
    }

    protected @Nullable StateType confirmChoice(int selectedChoice) {
        assert this.minecraft != null;
        if (this.menu.getAvailableChoices().get(selectedChoice).unavailabilityMessage().isPresent()) {
            return null;
        }

        StateType result = this.makeChoice(selectedChoice);

        switch (result) {
            case END_DIALOGUE -> this.minecraft.setScreen(null);
            case ASK_CONFIRMATION -> {
                ImmutableList<AvailableChoice> choices = this.menu.getAvailableChoices();
                this.minecraft.setScreen(new ConfirmScreen(
                        this::onBigChoiceMade,
                        this.menu.getCurrentText(),
                        Component.empty(),
                        choices.get(0).text(),
                        choices.get(1).text()
                ));
            }
            default -> {
                this.prepareLayout();
            }
        }

        return result;
    }

    private void onBigChoiceMade(boolean yes) {
        assert minecraft != null;
        if (this.confirmChoice(yes ? 0 : 1) == StateType.DEFAULT) {
            this.minecraft.setScreen(this);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.choiceList.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        return this.choiceList.mouseClicked(click, doubled);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float tickDelta) {
        super.render(context, mouseX, mouseY, tickDelta);

        assert minecraft != null;
        assert minecraft.player != null;

        PositionTransform positionTransform = this.createPositionTransform();
        positionTransform.setControlPoints(0, 0, this.width, this.height);

        for (String illustrationName : this.menu.getCurrentIllustrations()) {
            illustrations.getRenderer(illustrationName).render(context, this.font, positionTransform, mouseX, mouseY, tickDelta);
        }

        context.drawWordWrap(this.font, instructions, Math.max((this.width - this.font.width(instructions)) / 2, 5), instructionsMinY, this.width - 5, 0xFF808080, false);

        BlabberSettingsComponent settings = BlabberSettingsComponent.get(minecraft.player);
        if (settings.isDebugEnabled()) {
            positionTransform.setControlPoints(0, 0, this.width, this.height);
            renderDebugInfo(settings, context, positionTransform, mouseX, mouseY);
        }
    }

    protected @NotNull PositionTransform createPositionTransform() {
        return new PositionTransform(this.illustrationSlots);
    }

    protected void renderDebugInfo(BlabberSettingsComponent settings, GuiGraphics context, PositionTransform positionTransform, int mouseX, int mouseY) {
        if (settings.isEnabled(BlabberSetting.DEBUG_ANCHORS)) {
            this.renderAnchorDebugInfo(context, positionTransform, mouseX, mouseY);
        }
    }

    protected void renderAnchorDebugInfo(GuiGraphics context, PositionTransform positionTransform, int mouseX, int mouseY) {
        for (IllustrationAnchor anchor : IllustrationAnchor.values()) {
            int color = DEBUG_COLORS[anchor.ordinal() % DEBUG_COLORS.length];
            context.drawString(this.font, "x", positionTransform.transformX(anchor, -3), positionTransform.transformY(anchor, -5), color, true);
            MutableComponent text = Component.empty().append(Component.literal(anchor.getSerializedName()).withStyle(s -> s.withColor(color))).append(" > X: " + positionTransform.inverseTransformX(anchor, mouseX) + ", Y: " + positionTransform.inverseTransformY(anchor, mouseY));
            switch (anchor) {
                case TOP_LEFT, TOP_RIGHT -> context.setTooltipForNextFrame(
                        this.font,
                        text,
                        positionTransform.transformX(anchor, 0),
                        15
                );

                default -> context.setTooltipForNextFrame(
                        this.font,
                        text,
                        positionTransform.transformX(anchor, 0),
                        positionTransform.transformY(anchor, 0)
                );
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics matrices, float delta, int mouseX, int mouseY) {
        // NO-OP
    }

    @Override
    protected void renderLabels(GuiGraphics matrices, int mouseX, int mouseY) {
        // NO-OP
    }

    public StateType makeChoice(int choice) {
        int originalChoiceIndex = this.menu.getAvailableChoices().get(choice).originalChoiceIndex();
        StateType result = this.menu.makeChoice(originalChoiceIndex);
        ClientPlayNetworking.send(new ChoiceSelectionPayload((byte) originalChoiceIndex));
        return result;
    }

    @Override
    protected void updateNarrationState(NarrationElementOutput messageBuilder) {
        super.updateNarrationState(messageBuilder);
        messageBuilder.add(NarratedElementType.USAGE, instructions);
    }

    @Override
    protected boolean shouldNarrateNavigation() {
        return false;
    }
}
