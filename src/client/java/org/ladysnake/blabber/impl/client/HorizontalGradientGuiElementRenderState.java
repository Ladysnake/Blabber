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
package org.ladysnake.blabber.impl.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

@Environment(EnvType.CLIENT)
public record HorizontalGradientGuiElementRenderState(
        RenderPipeline pipeline,
        Matrix3x2f pose,
        int startX,
        int startY,
        int endX,
        int endY,
        int colorStart,
        int colorEnd,
        @Nullable ScreenRect scissorArea,
        @Nullable ScreenRect bounds
) implements SimpleGuiElementRenderState {
    public HorizontalGradientGuiElementRenderState(
            RenderPipeline pipeline, Matrix3x2f pose, int x0, int y0, int x1, int y1, int col1, int col2, @Nullable ScreenRect scissorArea
    ) {
        this(pipeline, pose, x0, y0, x1, y1, col1, col2, scissorArea, createBounds(x0, y0, x1, y1, pose, scissorArea));
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.empty();
    }

    @Override
    public void setupVertices(VertexConsumer vertices, float depth) {
//        vertices.vertex(this.pose(), (float)this.startX(), (float)this.startY(), depth).color(this.colorStart());
//        vertices.vertex(this.pose(), (float)this.startX(), (float)this.endY(), depth).color(this.colorEnd());
//        vertices.vertex(this.pose(), (float)this.endX(), (float)this.endY(), depth).color(this.colorEnd());
//        vertices.vertex(this.pose(), (float)this.endX(), (float)this.startY(), depth).color(this.colorStart());
        final int verticalPadding = 2;

        vertices.vertex(this.pose(), (float) startX, (float) startY - verticalPadding, depth).color(colorEnd);
        vertices.vertex(this.pose(), (float) startX, (float) startY, depth).color(colorStart);
        vertices.vertex(this.pose(), (float) endX, (float) startY, depth).color(colorEnd);
        vertices.vertex(this.pose(), (float) endX, (float) startY - verticalPadding, depth).color(colorEnd);

        vertices.vertex(this.pose(), (float) startX, (float) startY, depth).color(colorStart);
        vertices.vertex(this.pose(), (float) startX, (float) endY, depth).color(colorStart);
        vertices.vertex(this.pose(), (float) endX, (float) endY, depth).color(colorEnd);
        vertices.vertex(this.pose(), (float) endX, (float) startY, depth).color(colorEnd);

        vertices.vertex(this.pose(), (float) startX, (float) endY, depth).color(colorStart);
        vertices.vertex(this.pose(), (float) startX, (float) endY + verticalPadding, depth).color(colorEnd);
        vertices.vertex(this.pose(), (float) endX, (float) endY + verticalPadding, depth).color(colorEnd);
        vertices.vertex(this.pose(), (float) endX, (float) endY, depth).color(colorEnd);
//        vertices.vertex(this.pose(), (float) this.startX(), (float) this.startY(), depth).color(this.colorStart());
//        vertices.vertex(this.pose(), (float) this.startX(), (float) this.endY(), depth).color(this.colorEnd());
//        vertices.vertex(this.pose(), (float) this.endX(), (float) this.endY(), depth).color(this.colorEnd());
//        vertices.vertex(this.pose(), (float) this.endX(), (float) this.startY(), depth).color(this.colorStart());
    }

    @Nullable
    private static ScreenRect createBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRect scissorArea) {
        ScreenRect screenRect = new ScreenRect(x0, y0, x1 - x0, y1 - y0).transformEachVertex(pose);
        return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
    }
}
