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
package org.ladysnake.blabber.impl.common.model;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.Blabber;

public record DialogueChoiceCondition(Identifier predicate, UnavailableAction whenUnavailable) {
    public static final Codec<DialogueChoiceCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("predicate").forGetter(DialogueChoiceCondition::predicate),
            UnavailableAction.CODEC.fieldOf("when_unavailable").forGetter(DialogueChoiceCondition::whenUnavailable)
    ).apply(instance, DialogueChoiceCondition::new));
    public static final Identifier DUMMY_CONDITION = Blabber.id("client_dummy");

    public static void writeToPacket(PacketByteBuf buf, DialogueChoiceCondition condition) {
        UnavailableAction.writeToPacket(buf, condition.whenUnavailable());
    }

    public DialogueChoiceCondition(PacketByteBuf buf) {
        this(DUMMY_CONDITION, new UnavailableAction(buf));
    }

    public DialogueChoiceCondition parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) throws CommandSyntaxException {
        return new DialogueChoiceCondition(
                predicate(),
                whenUnavailable().parseText(source, sender)
        );
    }
}
