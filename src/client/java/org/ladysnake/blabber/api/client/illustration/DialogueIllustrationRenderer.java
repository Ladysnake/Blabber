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
package org.ladysnake.blabber.api.client.illustration;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.ladysnake.blabber.api.illustration.DialogueIllustration;
import org.ladysnake.blabber.api.illustration.DialogueIllustrationType;
import org.ladysnake.blabber.impl.client.BlabberClient;
import org.ladysnake.blabber.impl.common.illustrations.PositionTransform;

public abstract class DialogueIllustrationRenderer<I extends DialogueIllustration> {
    public static <I extends DialogueIllustration> void register(DialogueIllustrationType<I> type, DialogueIllustrationRenderer.Factory<I> factory) {
        BlabberClient.registerIllustrationRenderer(type, factory);
    }

    protected final I illustration;

    public DialogueIllustrationRenderer(I illustration) {
        this.illustration = illustration;
    }

    /**
     * Draw this illustration to the screen.
     *
     * @param context          a context to draw in
     * @param textRenderer     a text renderer
     * @param positionTransform an object that gives you real coordinates from illustration-local ones
     * @param mouseX           the current x mouse position
     * @param mouseY           the current y mouse position
     * @param tickDelta        how much time has passed since last frame
     */
    public abstract void render(DrawContext context, TextRenderer textRenderer, PositionTransform positionTransform, int mouseX, int mouseY, float tickDelta);

    @FunctionalInterface
    public interface Factory<I extends DialogueIllustration> {
        DialogueIllustrationRenderer<I> create(I illustration);
    }
}
