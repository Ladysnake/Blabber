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
package org.ladysnake.blabber.impl.common.illustrations;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.DialogueIllustration;

import java.util.Optional;

public abstract class DialogueIllustrationEntity<S extends DialogueIllustrationEntity.Spec> implements DialogueIllustration {
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
    @Environment(EnvType.CLIENT)
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY, float tickDelta) {
        LivingEntity e = this.renderedEntity == null
                ? this.renderedEntity = this.getRenderedEntity(MinecraftClient.getInstance().world)
                : this.renderedEntity;

        if (e == null) return; // Something went wrong creating the entity, so don't render.

        int fakedMouseX = spec.stareAtX().map(s -> s + x + (spec().x1() + spec().x2()) / 2).orElse(mouseX);
        int fakedMouseY = spec.stareAtY().map(s -> s + y + (spec().y1() + spec().y2()) / 2).orElse(mouseY);
        InventoryScreen.drawEntity(context,
                x + spec().x1(),
                y + spec().y1(),
                x + spec().x2(),
                y + spec().y2(),
                spec().size(),
                spec().yOff(),
                fakedMouseX,
                fakedMouseY,
                e);
    }

    public interface Spec {
        Optional<Integer> stareAtX();
        Optional<Integer> stareAtY();
        int x1();
        int y1();
        int x2();
        int y2();
        float yOff();
        int size();
    }
}