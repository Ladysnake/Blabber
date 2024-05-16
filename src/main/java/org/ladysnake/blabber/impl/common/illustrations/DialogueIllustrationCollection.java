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

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.api.DialogueIllustration;
import org.ladysnake.blabber.api.DialogueIllustrationType;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;

import java.util.ArrayList;
import java.util.List;

public record DialogueIllustrationCollection(List<DialogueIllustration> elements) implements DialogueIllustration {
    private static final Codec<DialogueIllustrationCollection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(DialogueIllustrationType.CODEC).fieldOf("elements").forGetter(DialogueIllustrationCollection::elements)
    ).apply(instance, DialogueIllustrationCollection::new));

    public static final DialogueIllustrationType<DialogueIllustrationCollection> TYPE = new DialogueIllustrationType<>(
            CODEC,
            buf -> new DialogueIllustrationCollection(buf.readList(b -> {
                DialogueIllustrationType<?> type = b.readRegistryValue(BlabberRegistrar.ILLUSTRATION_REGISTRY);
                assert type != null;
                return type.readFromPacket(b);
            })),
            (buf, item) ->
                buf.writeCollection(item.elements, (b, i) -> {
                    // Write the type, then the packet itself.
                    b.writeRegistryValue(BlabberRegistrar.ILLUSTRATION_REGISTRY, i.getType());
                    i.getType().writeToPacketUnsafe(b, i);
                })
    );

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY, float tickDelta) {
        for (DialogueIllustration i : elements) {
            i.render(context, textRenderer, x, y, mouseX, mouseY, tickDelta);
        }
    }

    @Override
    public DialogueIllustrationType<? extends DialogueIllustration> getType() {
        return TYPE;
    }

    @Override
    public DialogueIllustration parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) throws CommandSyntaxException {
        List<DialogueIllustration> parsedSub = new ArrayList<>(elements.size());
        for (DialogueIllustration illustration : elements) {
            parsedSub.add(illustration.parseText(source, sender));
        }
        return new DialogueIllustrationCollection(parsedSub);
    }
}
