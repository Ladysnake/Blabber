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
package org.ladysnake.blabber.api;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

/**
 * A renderable illustration in dialogues, such as entities or items.
 */
public interface DialogueIllustration {
    /**
     * Draw this illustration to the screen.
     *
     * @param context a context to draw in
     * @param textRenderer a text renderer
     * @param x the x position it should be drawn relative to
     * @param y the y position it should be drawn relative to
     * @param mouseX the current x mouse position
     * @param mouseY the current y mouse position
     * @param tickDelta how much time has passed since last frame
     */
    void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY, float tickDelta);

    /**
     * @return the DialogueIllustrationType that corresponds to this illustration
     */
    DialogueIllustrationType<? extends DialogueIllustration> getType();

    /**
     * If this illustration contains some text, this will be parsed *server-side*.
     * @param source the context in which this is parsed
     * @param sender the player that is going to dialogue
     * @return a DialogueIllustration with the required text parsed (often, simply this)
     */
    default DialogueIllustration parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) {
        return this;
    }
}
