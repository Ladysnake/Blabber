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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import org.ladysnake.blabber.api.DialogueIllustration;
import org.ladysnake.blabber.api.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;
import org.ladysnake.blabber.impl.common.serialization.OptionalSerialization;

import java.util.OptionalInt;

public record DialogueIllustrationTexture(
        Identifier texture,
        IllustrationAnchor anchor,
        int x,
        int y,
        int width,
        int height,
        OptionalInt u,
        OptionalInt v,
        OptionalInt textureWidth,
        OptionalInt textureHeight,
        OptionalInt regionWidth,
        OptionalInt regionHeight
) implements SizedDialogueIllustration {
    public static final Codec<DialogueIllustrationTexture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("texture").forGetter(DialogueIllustrationTexture::texture),
            Codecs.createStrictOptionalFieldCodec(IllustrationAnchor.CODEC, "anchor", IllustrationAnchor.TOP_LEFT).forGetter(DialogueIllustrationTexture::anchor),
            Codec.INT.fieldOf("x").forGetter(DialogueIllustrationTexture::x),
            Codec.INT.fieldOf("y").forGetter(DialogueIllustrationTexture::y),
            Codec.INT.fieldOf("width").forGetter(DialogueIllustrationTexture::width),
            Codec.INT.fieldOf("height").forGetter(DialogueIllustrationTexture::height),
            OptionalSerialization.optionalIntField("u").forGetter(DialogueIllustrationTexture::u),
            OptionalSerialization.optionalIntField("v").forGetter(DialogueIllustrationTexture::v),
            OptionalSerialization.optionalIntField("texture_width").forGetter(DialogueIllustrationTexture::textureWidth),
            OptionalSerialization.optionalIntField("texture_height").forGetter(DialogueIllustrationTexture::textureHeight),
            OptionalSerialization.optionalIntField("region_width").forGetter(DialogueIllustrationTexture::regionWidth),
            OptionalSerialization.optionalIntField("region_height").forGetter(DialogueIllustrationTexture::regionHeight)
    ).apply(instance, DialogueIllustrationTexture::new));

    public static final DialogueIllustrationType<DialogueIllustrationTexture> TYPE = new DialogueIllustrationType<>(CODEC,
            buf -> new DialogueIllustrationTexture(
                    buf.readIdentifier(),
                    buf.readEnumConstant(IllustrationAnchor.class),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    OptionalSerialization.readOptionalInt(buf),
                    OptionalSerialization.readOptionalInt(buf),
                    OptionalSerialization.readOptionalInt(buf),
                    OptionalSerialization.readOptionalInt(buf),
                    OptionalSerialization.readOptionalInt(buf),
                    OptionalSerialization.readOptionalInt(buf)
            ),
            (buf, image) -> {
                buf.writeIdentifier(image.texture());
                buf.writeEnumConstant(image.anchor());
                buf.writeVarInt(image.x());
                buf.writeVarInt(image.y());
                buf.writeVarInt(image.width());
                buf.writeVarInt(image.height());
                OptionalSerialization.writeOptionalInt(buf, image.u());
                OptionalSerialization.writeOptionalInt(buf, image.v());
                OptionalSerialization.writeOptionalInt(buf, image.textureWidth());
                OptionalSerialization.writeOptionalInt(buf, image.textureHeight());
                OptionalSerialization.writeOptionalInt(buf, image.regionWidth());
                OptionalSerialization.writeOptionalInt(buf, image.regionHeight());
            }
    );

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, PositionTransform positionTransform, int mouseX, int mouseY, float tickDelta) {
        context.drawTexture(
                texture(),
                minX(positionTransform),
                minY(positionTransform),
                width(),
                height(),
                0,
                0,
                regionWidth().orElse(width()),
                regionHeight().orElse(height()),
                textureWidth().orElse(width()),
                textureHeight().orElse(height())
        );
    }

    @Override
    public DialogueIllustrationType<? extends DialogueIllustration> getType() {
        return TYPE;
    }
}
