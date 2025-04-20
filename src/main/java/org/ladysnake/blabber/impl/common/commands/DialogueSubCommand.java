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
package org.ladysnake.blabber.impl.common.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;
import org.ladysnake.blabber.impl.common.DialogueRegistry;
import org.ladysnake.blabber.impl.common.PlayerDialogueTracker;

import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class DialogueSubCommand {
    public static final DynamicCommandExceptionType INVALID_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("blabber:commands.dialogue.start.invalid", id.toString()));

    public static final String DIALOGUE_SUBCOMMAND = "dialogue";

    static LiteralArgumentBuilder<ServerCommandSource> dialogueSubtree() {
        return literal(DIALOGUE_SUBCOMMAND)
                .then(dialogueStartCommand());
    }

    /**
     * /blabber dialogue start <dialogue> [players] [interlocutor]
     */
    private static LiteralArgumentBuilder<ServerCommandSource> dialogueStartCommand() {
        return literal("start")
                .requires(Permissions.require("dialogue.start", 2))
                .then(argument("dialogue", IdentifierArgumentType.identifier()).suggests(BlabberRegistrar.ALL_DIALOGUES)
                        .executes(context -> startDialogue(context.getSource(), IdentifierArgumentType.getIdentifier(context, "dialogue"), List.of(context.getSource().getPlayerOrThrow()), null))
                        .then(argument("players", EntityArgumentType.players())
                                .executes(context -> startDialogue(context.getSource(), IdentifierArgumentType.getIdentifier(context, "dialogue"), EntityArgumentType.getPlayers(context, "players"), null))
                                .then(argument("interlocutor", EntityArgumentType.entity())
                                        .executes(context -> startDialogue(context.getSource(), IdentifierArgumentType.getIdentifier(context, "dialogue"), EntityArgumentType.getPlayers(context, "players"), EntityArgumentType.getEntity(context, "interlocutor")))
                                )
                        )
                );
    }

    private static int startDialogue(ServerCommandSource source, Identifier dialogue, Collection<ServerPlayerEntity> players, @Nullable Entity interlocutor) throws CommandSyntaxException {
        if (!DialogueRegistry.containsId(dialogue)) {
            throw INVALID_EXCEPTION.create(dialogue);
        }

        int count = 0;
        for (ServerPlayerEntity player : players) {
            PlayerDialogueTracker.get(player).startDialogue(dialogue, interlocutor);
            source.sendFeedback(() -> Text.translatable("blabber:commands.dialogue.start.success", dialogue.toString(), player.getDisplayName()), true);
            count++;
        }

        return count;
    }
}
