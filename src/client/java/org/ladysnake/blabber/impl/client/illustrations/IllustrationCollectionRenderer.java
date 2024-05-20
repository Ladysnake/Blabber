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
package org.ladysnake.blabber.impl.client.illustrations;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.ladysnake.blabber.api.client.illustration.DialogueIllustrationRenderer;
import org.ladysnake.blabber.api.illustration.DialogueIllustration;
import org.ladysnake.blabber.impl.client.BlabberClient;
import org.ladysnake.blabber.impl.common.illustrations.DialogueIllustrationCollection;
import org.ladysnake.blabber.impl.common.illustrations.PositionTransform;

import java.util.ArrayList;
import java.util.List;

public class IllustrationCollectionRenderer extends DialogueIllustrationRenderer<DialogueIllustrationCollection> {
    private final List<DialogueIllustrationRenderer<?>> elements;

    public IllustrationCollectionRenderer(DialogueIllustrationCollection illustration) {
        super(illustration);
        this.elements = new ArrayList<>();
        for (DialogueIllustration element : illustration.elements()) {
            this.elements.add(BlabberClient.createRenderer(element));
        }
    }

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, PositionTransform positionTransform, int mouseX, int mouseY, float tickDelta) {
        for (DialogueIllustrationRenderer<?> i : elements) {
            i.render(context, textRenderer, positionTransform, mouseX, mouseY, tickDelta);
        }
    }
}
