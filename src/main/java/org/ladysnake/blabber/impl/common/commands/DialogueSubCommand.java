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
package org.ladysnake.blabber.impl.common.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.blabber.impl.common.BlabberRegistrar;
import org.ladysnake.blabber.impl.common.DialogueRegistry;
import org.ladysnake.blabber.impl.common.PlayerDialogueTracker;

import java.util.Collection;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class DialogueSubCommand {
    public static final DynamicCommandExceptionType INVALID_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("blabber:commands.dialogue.start.invalid", id.toString()));

    public static final String DIALOGUE_SUBCOMMAND = "dialogue";

    static LiteralArgumentBuilder<CommandSourceStack> dialogueSubtree() {
        return literal(DIALOGUE_SUBCOMMAND)
                .then(dialogueStartCommand());
    }

    /**
     * /blabber dialogue start <dialogue> [players] [interlocutor]
     */
    private static LiteralArgumentBuilder<CommandSourceStack> dialogueStartCommand() {
        return literal("start")
                .requires(Permissions.require("dialogue.start", PermissionLevel.GAMEMASTERS))
                .then(argument("dialogue", IdentifierArgument.id()).suggests(BlabberRegistrar.ALL_DIALOGUES)
                        .executes(context -> startDialogue(context.getSource(), IdentifierArgument.getId(context, "dialogue"), List.of(context.getSource().getPlayerOrException()), null))
                        .then(argument("players", EntityArgument.players())
                                .executes(context -> startDialogue(context.getSource(), IdentifierArgument.getId(context, "dialogue"), EntityArgument.getPlayers(context, "players"), null))
                                .then(argument("interlocutor", EntityArgument.entity())
                                        .executes(context -> startDialogue(context.getSource(), IdentifierArgument.getId(context, "dialogue"), EntityArgument.getPlayers(context, "players"), EntityArgument.getEntity(context, "interlocutor")))
                                )
                        )
                );
    }

    private static int startDialogue(CommandSourceStack source, Identifier dialogue, Collection<ServerPlayer> players, @Nullable Entity interlocutor) throws CommandSyntaxException {
        if (!DialogueRegistry.containsId(dialogue)) {
            throw INVALID_EXCEPTION.create(dialogue);
        }

        int count = 0;
        for (ServerPlayer player : players) {
            PlayerDialogueTracker.get(player).startDialogue(dialogue, interlocutor);
            source.sendSuccess(() -> Component.translatable("blabber:commands.dialogue.start.success", dialogue.toString(), player.getDisplayName()), true);
            count++;
        }

        return count;
    }
}
