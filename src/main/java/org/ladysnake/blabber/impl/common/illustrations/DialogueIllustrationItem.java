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
import org.ladysnake.blabber.api.DialogueIllustration;
import org.ladysnake.blabber.api.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.FailingOptionalFieldCodec;

public record DialogueIllustrationItem(ItemStack stack, int x, int y, boolean showTooltip) implements DialogueIllustration {
    private static final Codec<DialogueIllustrationItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(DialogueIllustrationItem::stack),
            Codec.INT.fieldOf("x").forGetter(DialogueIllustrationItem::x),
            Codec.INT.fieldOf("y").forGetter(DialogueIllustrationItem::y),
            FailingOptionalFieldCodec.of(Codec.BOOL, "show_tooltip", true).forGetter(DialogueIllustrationItem::showTooltip)
    ).apply(instance, DialogueIllustrationItem::new));

    public static final DialogueIllustrationType<DialogueIllustrationItem> TYPE = new DialogueIllustrationType<>(
            CODEC,
            buf -> new DialogueIllustrationItem(ItemStack.fromNbt(buf.readNbt()), buf.readInt(), buf.readInt(), buf.readBoolean()),
            (buf, item) -> {
                buf.writeNbt(item.stack().writeNbt(new NbtCompound()));
                buf.writeInt(item.x());
                buf.writeInt(item.y());
                buf.writeBoolean(item.showTooltip());
            }
    );

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY, float tickDelta) {
        // We draw the actual item, then the count and bar and such.
        context.drawItem(stack, this.x + x, this.y + y);
        context.drawItemInSlot(textRenderer, stack, this.x + x, this.y + y);
        if (showTooltip &&
                this.x + x <= mouseX && this.x + x + 20 > mouseX &&
                this.y + y <= mouseY && this.y + y + 20 > mouseY)
            context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
    }

    @Override
    public DialogueIllustrationType<?> getType() {
        return TYPE;
    }
}
