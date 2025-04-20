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
package org.ladysnake.blabber.impl.common.model;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.Blabber;

public record DialogueChoiceCondition(RegistryKey<LootCondition> predicate, UnavailableAction whenUnavailable) {
    public static final RegistryKey<LootCondition> DUMMY_CONDITION = RegistryKey.of(RegistryKeys.PREDICATE, Blabber.id("client_dummy"));
    public static final Codec<DialogueChoiceCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryKey.createCodec(RegistryKeys.PREDICATE).fieldOf("predicate").forGetter(DialogueChoiceCondition::predicate),
            UnavailableAction.CODEC.fieldOf("when_unavailable").forGetter(DialogueChoiceCondition::whenUnavailable)
    ).apply(instance, DialogueChoiceCondition::new));
    public static final PacketCodec<PacketByteBuf, DialogueChoiceCondition> PACKET_CODEC = PacketCodec.tuple(
            // Not writing the condition, it is handled serverside
            PacketCodec.of((value, buf) -> {}, buf -> DUMMY_CONDITION), DialogueChoiceCondition::predicate,
            UnavailableAction.PACKET_CODEC, DialogueChoiceCondition::whenUnavailable,
            DialogueChoiceCondition::new
    );

    public DialogueChoiceCondition parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) throws CommandSyntaxException {
        return new DialogueChoiceCondition(
                predicate(),
                whenUnavailable().parseText(source, sender)
        );
    }
}
