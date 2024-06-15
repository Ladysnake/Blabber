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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.ladysnake.blabber.api.illustration.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.model.IllustrationAnchor;

public record DialogueIllustrationItem(ItemStack stack, IllustrationAnchor anchor, int x, int y, float scale,
                                       boolean showTooltip) implements SizedDialogueIllustration {
    private static final MapCodec<DialogueIllustrationItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(DialogueIllustrationItem::stack),
            IllustrationAnchor.CODEC.optionalFieldOf("anchor", IllustrationAnchor.TOP_LEFT).forGetter(DialogueIllustrationItem::anchor),
            Codec.INT.fieldOf("x").forGetter(DialogueIllustrationItem::x),
            Codec.INT.fieldOf("y").forGetter(DialogueIllustrationItem::y),
            Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(DialogueIllustrationItem::scale),
            Codec.BOOL.optionalFieldOf("show_tooltip", true).forGetter(DialogueIllustrationItem::showTooltip)
    ).apply(instance, DialogueIllustrationItem::new));
    public static final PacketCodec<RegistryByteBuf, DialogueIllustrationItem> PACKET_CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC, DialogueIllustrationItem::stack,
            IllustrationAnchor.PACKET_CODEC, DialogueIllustrationItem::anchor,
            PacketCodecs.VAR_INT, DialogueIllustrationItem::x,
            PacketCodecs.VAR_INT, DialogueIllustrationItem::y,
            PacketCodecs.FLOAT, DialogueIllustrationItem::scale,
            PacketCodecs.BOOL, DialogueIllustrationItem::showTooltip,
            DialogueIllustrationItem::new
    );

    public static final DialogueIllustrationType<DialogueIllustrationItem> TYPE = new DialogueIllustrationType<>(
            CODEC,
            PACKET_CODEC
    );

    @Override
    public int width() {
        return Math.round(16 * (this.scale)) + 4;
    }

    @Override
    public int height() {
        return Math.round(16 * (this.scale)) + 4;
    }

    @Override
    public DialogueIllustrationType<?> getType() {
        return TYPE;
    }
}
