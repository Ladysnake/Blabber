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
package org.ladysnake.blabber.impl.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.blabber.DialogueAction;

public record CommandDialogueAction(String command) implements DialogueAction {
    public static final MapCodec<CommandDialogueAction> CODEC = Codec.STRING.xmap(CommandDialogueAction::new, CommandDialogueAction::command).fieldOf("value");

    @Override
    public void handle(ServerPlayerEntity player) {
        player.server.getCommandManager().executeWithPrefix(
                getSource(player),
                this.command()
        );
    }

    public static ServerCommandSource getSource(ServerPlayerEntity player) {
        return player.getCommandSource()
                .withOutput(player.server)
                .withLevel(2);
    }
}
