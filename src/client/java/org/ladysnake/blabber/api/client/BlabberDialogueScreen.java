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
package org.ladysnake.blabber.api.client;

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
public class BlabberDialogueScreen<P extends DialogueLayout.Params> extends HandledScreen<DialogueScreenHandler> {
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

    protected final Text instructions;

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

    // Things that are mutated during state changes
    protected int choiceListMinY;

    // Widgets
    protected DialogueTextWidget title;
    protected DialogueChoiceListWidget choiceList;

    protected final IllustrationContainer illustrations = new IllustrationContainer();

    public BlabberDialogueScreen(DialogueScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        GameOptions options = MinecraftClient.getInstance().options;
        this.instructions = Text.translatable("blabber:dialogue.instructions", options.forwardKey.getBoundKeyLocalizedText(), options.backKey.getBoundKeyLocalizedText(), options.inventoryKey.getBoundKeyLocalizedText());
        this.illustrationSlots = new EnumMap<>(IllustrationAnchor.class);
        for (IllustrationAnchor anchor : IllustrationAnchor.values()) {
            this.illustrationSlots.put(anchor, new Vector2i(-999, -999));
        }
    }

    @SuppressWarnings("unchecked")
    protected P params() {
        return (P) this.handler.getLayout().params();
    }

    @Override
    protected void init() {
        super.init();
        this.title = this.addDrawableChild(new DialogueTextWidget(0, 0, mainTextMaxWidth, mainTextMaxRows, Text.empty(), this.textRenderer));
        this.choiceList = this.addDrawableChild(createChoiceList());
        this.prepareLayout();
        this.illustrations.setIllustrations(this.handler.getIllustrations());
    }

    protected @NotNull DialogueChoiceListWidget createChoiceList() {
        return new DialogueChoiceListWidget(0, 0, choiceListMaxWidth, 1000, Text.empty(), textRenderer, this::confirmChoice, illustrations);
    }

    protected void prepareLayout() {
        this.computeMargins();
        this.layoutIllustrationAnchors();
        this.title.setPosition(mainTextMinX, mainTextMinY);
        this.title.setTextWidth(mainTextMaxWidth);
        this.title.setTextColor(mainTextColor);
        this.title.setMessage(handler.getCurrentText());
        this.choiceList.setChoices(handler.getAvailableChoices());
        this.positionChoiceList();
    }

    protected void positionChoiceList() {
        this.choiceList.setPosition(choiceListMinX, this.title.getY() + Math.min(this.title.getContentsHeightWithPadding(), this.title.getHeight()) + DEFAULT_TITLE_GAP);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        GameOptions options = MinecraftClient.getInstance().options;
        if (options.forwardKey.matchesKey(keyCode, scanCode) || options.backKey.matchesKey(keyCode, scanCode) || options.inventoryKey.matchesKey(keyCode, scanCode)) {
            this.setFocused(this.choiceList);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !this.handler.isUnskippable();
    }

    protected @Nullable StateType confirmChoice(int selectedChoice) {
        assert this.client != null;
        if (this.handler.getAvailableChoices().get(selectedChoice).unavailabilityMessage().isPresent()) {
            return null;
        }

        StateType result = this.makeChoice(selectedChoice);

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
                this.prepareLayout();
            }
        }

        return result;
    }

    private void onBigChoiceMade(boolean yes) {
        assert client != null;
        if (this.confirmChoice(yes ? 0 : 1) == StateType.DEFAULT) {
            this.client.setScreen(this);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.choiceList.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.choiceList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        super.render(context, mouseX, mouseY, tickDelta);

        assert client != null;
        assert client.player != null;

        PositionTransform positionTransform = this.createPositionTransform();
        positionTransform.setControlPoints(0, 0, this.width, this.height);

        for (String illustrationName : this.handler.getCurrentIllustrations()) {
            illustrations.getRenderer(illustrationName).render(context, this.textRenderer, positionTransform, mouseX, mouseY, tickDelta);
        }

        context.drawWrappedText(this.textRenderer, instructions, Math.max((this.width - this.textRenderer.getWidth(instructions)) / 2, 5), instructionsMinY, this.width - 5, 0xFF808080, false);

        BlabberSettingsComponent settings = BlabberSettingsComponent.get(client.player);
        if (settings.isDebugEnabled()) {
            positionTransform.setControlPoints(0, 0, this.width, this.height);
            renderDebugInfo(settings, context, positionTransform, mouseX, mouseY);
        }
    }

    protected @NotNull PositionTransform createPositionTransform() {
        return new PositionTransform(this.illustrationSlots);
    }

    protected void renderDebugInfo(BlabberSettingsComponent settings, DrawContext context, PositionTransform positionTransform, int mouseX, int mouseY) {
        if (settings.isEnabled(BlabberSetting.DEBUG_ANCHORS)) {
            this.renderAnchorDebugInfo(context, positionTransform, mouseX, mouseY);
        }
    }

    protected void renderAnchorDebugInfo(DrawContext context, PositionTransform positionTransform, int mouseX, int mouseY) {
        for (IllustrationAnchor anchor : IllustrationAnchor.values()) {
            int color = DEBUG_COLORS[anchor.ordinal() % DEBUG_COLORS.length];
            context.drawText(this.textRenderer, "x", positionTransform.transformX(anchor, -3), positionTransform.transformY(anchor, -5), color, true);
            MutableText text = Text.empty().append(Text.literal(anchor.asString()).styled(s -> s.withColor(color))).append(" > X: " + positionTransform.inverseTransformX(anchor, mouseX) + ", Y: " + positionTransform.inverseTransformY(anchor, mouseY));
            switch (anchor) {
                case TOP_LEFT, TOP_RIGHT -> context.drawTooltip(
                        this.textRenderer,
                        text,
                        positionTransform.transformX(anchor, 0),
                        15
                );

                default -> context.drawTooltip(
                        this.textRenderer,
                        text,
                        positionTransform.transformX(anchor, 0),
                        positionTransform.transformY(anchor, 0)
                );
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

    public StateType makeChoice(int choice) {
        int originalChoiceIndex = this.handler.getAvailableChoices().get(choice).originalChoiceIndex();
        StateType result = this.handler.makeChoice(originalChoiceIndex);
        ClientPlayNetworking.send(new ChoiceSelectionPayload((byte) originalChoiceIndex));
        return result;
    }

    @Override
    protected void addScreenNarrations(NarrationMessageBuilder messageBuilder) {
        super.addScreenNarrations(messageBuilder);
        messageBuilder.put(NarrationPart.USAGE, instructions);
    }

    @Override
    protected boolean hasUsageText() {
        return false;
    }
}
