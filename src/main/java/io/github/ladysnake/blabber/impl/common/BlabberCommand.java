/*
 * Blabber
 * Copyright (C) 2022 Ladysnake
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
package io.github.ladysnake.blabber.impl.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.github.ladysnake.blabber.Blabber;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class BlabberCommand {
    public static final DynamicCommandExceptionType INVALID_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("blabber:commands.dialogue.start.invalid", id));

    public static final String DIALOGUE_SUBCOMMAND = "dialogue";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(Blabber.MOD_ID)
            .requires(Permissions.require("dialogue.start", 2))
            .then(literal(DIALOGUE_SUBCOMMAND)
            // blabber dialogue start <dialogue> [players]
            .then(literal("start")
                .requires(Permissions.require("dialogue.start", 2))
                .then(argument("dialogue", IdentifierArgumentType.identifier()).suggests(BlabberRegistrar.ALL_DIALOGUES)
                    .executes(context -> startDialogue(context.getSource(), IdentifierArgumentType.getIdentifier(context, "dialogue"), List.of(context.getSource().getPlayer())))
                    .then(argument("players", EntityArgumentType.players())
                        .executes(context -> startDialogue(context.getSource(), IdentifierArgumentType.getIdentifier(context, "dialogue"), EntityArgumentType.getPlayers(context, "players")))
                    )
                )
            )));
    }

    private static int startDialogue(ServerCommandSource source, Identifier dialogue, Collection<ServerPlayerEntity> players) throws CommandSyntaxException {
        if (!source.getServer().getRegistryManager().get(BlabberRegistrar.DIALOGUE_REGISTRY_KEY).containsId(dialogue)) {
            throw INVALID_EXCEPTION.create(dialogue);
        }

        int count = 0;
        for (ServerPlayerEntity player : players) {
            PlayerDialogueTracker.get(player).startDialogue(dialogue);
            source.sendFeedback(Text.translatable("blabber:commands.dialogue.start.success", dialogue, player.getDisplayName()), true);
            count++;
        }

        return count;
    }
}
