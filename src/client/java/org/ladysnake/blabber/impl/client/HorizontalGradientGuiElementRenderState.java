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
package org.ladysnake.blabber.impl.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

@Environment(EnvType.CLIENT)
public record HorizontalGradientGuiElementRenderState(
        RenderPipeline pipeline,
        Matrix3x2fc pose,
        int startX,
        int startY,
        int endX,
        int endY,
        int colorStart,
        int colorEnd,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
    public HorizontalGradientGuiElementRenderState(
            RenderPipeline pipeline, Matrix3x2f pose, int x0, int y0, int x1, int y1, int col1, int col2, @Nullable ScreenRectangle scissorArea
    ) {
        this(pipeline, pose, x0, y0, x1, y1, col1, col2, scissorArea, createBounds(x0, y0, x1, y1, pose, scissorArea));
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public void buildVertices(VertexConsumer vertices) {
//        vertices.vertex(this.pose(), (float)this.startX(), (float)this.startY()).color(this.colorStart());
//        vertices.vertex(this.pose(), (float)this.startX(), (float)this.endY()).color(this.colorEnd());
//        vertices.vertex(this.pose(), (float)this.endX(), (float)this.endY()).color(this.colorEnd());
//        vertices.vertex(this.pose(), (float)this.endX(), (float)this.startY()).color(this.colorStart());
        final int verticalPadding = 2;
        final float depth = 0;

        vertices.addVertexWith2DPose(this.pose(), startX, startY - verticalPadding).setColor(colorEnd);
        vertices.addVertexWith2DPose(this.pose(), startX, startY).setColor(colorStart);
        vertices.addVertexWith2DPose(this.pose(), endX, startY).setColor(colorEnd);
        vertices.addVertexWith2DPose(this.pose(), endX, startY - verticalPadding).setColor(colorEnd);

        vertices.addVertexWith2DPose(this.pose(), startX, startY).setColor(colorStart);
        vertices.addVertexWith2DPose(this.pose(), startX, endY).setColor(colorStart);
        vertices.addVertexWith2DPose(this.pose(), endX, endY).setColor(colorEnd);
        vertices.addVertexWith2DPose(this.pose(), endX, startY).setColor(colorEnd);

        vertices.addVertexWith2DPose(this.pose(), startX, endY).setColor(colorStart);
        vertices.addVertexWith2DPose(this.pose(), startX, endY + verticalPadding).setColor(colorEnd);
        vertices.addVertexWith2DPose(this.pose(), endX, endY + verticalPadding).setColor(colorEnd);
        vertices.addVertexWith2DPose(this.pose(), endX, endY).setColor(colorEnd);
//        vertices.vertex(this.pose(), this.startX(), this.startY()).color(this.colorStart());
//        vertices.vertex(this.pose(), this.startX(), this.endY()).color(this.colorEnd());
//        vertices.vertex(this.pose(), this.endX(), this.endY()).color(this.colorEnd());
//        vertices.vertex(this.pose(), this.endX(), this.startY()).color(this.colorStart());
    }

    @Nullable
    private static ScreenRectangle createBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle screenRect = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
    }
}
