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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.dynamic.Codecs;
import org.ladysnake.blabber.api.DialogueIllustration;
import org.ladysnake.blabber.api.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;

public record DialogueIllustrationItem(ItemStack stack, IllustrationAnchor anchor, int x, int y, float scale,
                                       boolean showTooltip) implements DialogueIllustration {
    private static final Codec<DialogueIllustrationItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(DialogueIllustrationItem::stack),
            Codecs.createStrictOptionalFieldCodec(IllustrationAnchor.CODEC, "anchor", IllustrationAnchor.TOP_LEFT).forGetter(DialogueIllustrationItem::anchor),
            Codec.INT.fieldOf("x").forGetter(DialogueIllustrationItem::x),
            Codec.INT.fieldOf("y").forGetter(DialogueIllustrationItem::y),
            Codecs.createStrictOptionalFieldCodec(Codec.FLOAT, "scale", 1f).forGetter(DialogueIllustrationItem::scale),
            Codecs.createStrictOptionalFieldCodec(Codec.BOOL, "show_tooltip", true).forGetter(DialogueIllustrationItem::showTooltip)
    ).apply(instance, DialogueIllustrationItem::new));

    public static final DialogueIllustrationType<DialogueIllustrationItem> TYPE = new DialogueIllustrationType<>(
            CODEC,
            buf -> new DialogueIllustrationItem(ItemStack.fromNbt(buf.readNbt()), buf.readEnumConstant(IllustrationAnchor.class), buf.readInt(), buf.readInt(), buf.readFloat(), buf.readBoolean()),
            (buf, item) -> {
                buf.writeNbt(item.stack().writeNbt(new NbtCompound()));
                buf.writeEnumConstant(item.anchor());
                buf.writeInt(item.x());
                buf.writeInt(item.y());
                buf.writeFloat(item.scale());
                buf.writeBoolean(item.showTooltip());
            }
    );

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, PositionTransform positionTransform, int mouseX, int mouseY, float tickDelta) {
        // We draw the actual item, then the count and bar and such.
        try {
            ((DrawContextHooks) context).blabber$setItemScale(scale);
            int originX = positionTransform.transformX(this.anchor, this.x);
            int originY = positionTransform.transformY(this.anchor, this.y);
            context.drawItem(stack, originX, originY);
            if (scale() == 1) {  // Not supporting rescaled stack decorations right now
                context.drawItemInSlot(textRenderer, stack, originX, originY);
            }
            if (showTooltip &&
                    originX - (8 * (this.scale - 1)) <= mouseX && originX + (8 * (this.scale + 1)) + 4 > mouseX &&
                    originY - (8 * (this.scale - 1)) <= mouseY && originY + (8 * (this.scale + 1)) + 4 > mouseY) {
                context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
            }
        } finally {
            ((DrawContextHooks) context).blabber$setItemScale(1f);
        }
    }

    @Override
    public DialogueIllustrationType<?> getType() {
        return TYPE;
    }

    public interface DrawContextHooks {
        void blabber$setItemScale(float itemScale);
    }
}
