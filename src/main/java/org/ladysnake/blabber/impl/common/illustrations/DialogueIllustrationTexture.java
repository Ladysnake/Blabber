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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import org.joml.Matrix4f;
import org.ladysnake.blabber.api.DialogueIllustration;
import org.ladysnake.blabber.api.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;

public record DialogueIllustrationTexture(
        Identifier texture,
        IllustrationAnchor anchor,
        int x1,
        int y1,
        int x2,
        int y2,
        int z,
        float u1,
        float v1,
        float u2,
        float v2
) implements DialogueIllustration {
    public static final Codec<DialogueIllustrationTexture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("texture").forGetter(DialogueIllustrationTexture::texture),
            Codecs.createStrictOptionalFieldCodec(IllustrationAnchor.CODEC, "anchor", IllustrationAnchor.TOP_LEFT).forGetter(DialogueIllustrationTexture::anchor),
            Codec.INT.fieldOf("x1").forGetter(DialogueIllustrationTexture::x1),
            Codec.INT.fieldOf("y1").forGetter(DialogueIllustrationTexture::y1),
            Codec.INT.fieldOf("x2").forGetter(DialogueIllustrationTexture::x2),
            Codec.INT.fieldOf("y2").forGetter(DialogueIllustrationTexture::y2),
            Codec.INT.optionalFieldOf("z", 0).forGetter(DialogueIllustrationTexture::z),
            Codec.FLOAT.optionalFieldOf("u1", 0f).forGetter(DialogueIllustrationTexture::u1),
            Codec.FLOAT.optionalFieldOf("v1", 0f).forGetter(DialogueIllustrationTexture::v1),
            Codec.FLOAT.optionalFieldOf("u2", 1f).forGetter(DialogueIllustrationTexture::u2),
            Codec.FLOAT.optionalFieldOf("v2", 1f).forGetter(DialogueIllustrationTexture::v2)
    ).apply(instance, DialogueIllustrationTexture::new));
    public static final DialogueIllustrationType<DialogueIllustrationTexture> TYPE = new DialogueIllustrationType<>(CODEC,
            buf -> new DialogueIllustrationTexture(
                    buf.readIdentifier(),
                    buf.readEnumConstant(IllustrationAnchor.class),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat()
            ),
            (buf, image) -> {
                buf.writeIdentifier(image.texture());
                buf.writeEnumConstant(image.anchor());
                buf.writeVarInt(image.x1());
                buf.writeVarInt(image.y1());
                buf.writeVarInt(image.x2());
                buf.writeVarInt(image.y2());
                buf.writeVarInt(image.z());
                buf.writeFloat(image.u1());
                buf.writeFloat(image.v1());
                buf.writeFloat(image.u2());
                buf.writeFloat(image.v2());
            }
    );

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, PositionTransform positionTransform, int mouseX, int mouseY, float tickDelta) {
        drawTexturedQuad(
                context.getMatrices(),
                texture(),
                positionTransform.transformX(anchor(), x1()),
                positionTransform.transformX(anchor(), x2()),
                positionTransform.transformY(anchor(), y1()),
                positionTransform.transformY(anchor(), y2()),
                z(),
                u1(),
                u2(),
                v1(),
                v2()
        );
    }

    private void drawTexturedQuad(MatrixStack matrices, Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f, x1, y1, z).texture(u1, v1).next();
        bufferBuilder.vertex(matrix4f, x1, y2, z).texture(u1, v2).next();
        bufferBuilder.vertex(matrix4f, x2, y2, z).texture(u2, v2).next();
        bufferBuilder.vertex(matrix4f, x2, y1, z).texture(u2, v1).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    @Override
    public DialogueIllustrationType<? extends DialogueIllustration> getType() {
        return TYPE;
    }
}
