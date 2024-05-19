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
package org.ladysnake.blabber.impl.common.illustrations.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.impl.common.illustrations.PositionTransform;
import org.ladysnake.blabber.impl.common.illustrations.SizedDialogueIllustration;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;

public abstract class DialogueIllustrationEntity<S extends DialogueIllustrationEntity.Spec> implements SizedDialogueIllustration {
    private final S spec;
    private transient @Nullable LivingEntity renderedEntity;

    protected DialogueIllustrationEntity(S spec) {
        this.spec = spec;
    }

    public S spec() {
        return spec;
    }

    protected abstract @Nullable LivingEntity getRenderedEntity(World world);

    @Override
    public IllustrationAnchor anchor() {
        return spec().anchor();
    }

    @Override
    public int x() {
        return spec().x();
    }

    @Override
    public int y() {
        return spec().y();
    }

    @Override
    public int width() {
        return spec().width();
    }

    @Override
    public int height() {
        return spec().height();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(DrawContext context, TextRenderer textRenderer, PositionTransform positionTransform, int mouseX, int mouseY, float tickDelta) {
        LivingEntity e = this.renderedEntity == null
                ? this.renderedEntity = this.getRenderedEntity(MinecraftClient.getInstance().world)
                : this.renderedEntity;

        if (e == null) return; // Something went wrong creating the entity, so don't render.

        int x1 = minX(positionTransform);
        int y1 = minY(positionTransform);
        int x2 = maxX(positionTransform);
        int y2 = maxY(positionTransform);

        StareTarget stareTarget = spec().stareAt();
        int fakedMouseX = stareTarget.x().isPresent() ? stareTarget.anchor().isPresent() ? positionTransform.transformX(stareTarget.anchor().get(), stareTarget.x().getAsInt()) : stareTarget.x().getAsInt() + (x1 + x2) / 2 : mouseX;
        int fakedMouseY = stareTarget.y().isPresent() ? stareTarget.anchor().isPresent() ? positionTransform.transformY(stareTarget.anchor().get(), stareTarget.y().getAsInt()) : stareTarget.y().getAsInt() + (y1 + y2) / 2 : mouseY;

        InventoryScreen.drawEntity(context,
                x1,
                y1,
                x2,
                y2,
                spec().entitySize(),
                spec().yOffset(),
                fakedMouseX,
                fakedMouseY,
                e);
    }

    public interface Spec {
        IllustrationAnchor anchor();
        StareTarget stareAt();
        int x();
        int y();
        int width();
        int height();
        float yOffset();
        int entitySize();
    }
}
