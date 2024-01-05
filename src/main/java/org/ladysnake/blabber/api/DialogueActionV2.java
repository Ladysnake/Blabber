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
package org.ladysnake.blabber.api;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * @see org.ladysnake.blabber.Blabber#registerAction(Identifier, DialogueActionV2)
 * @see org.ladysnake.blabber.Blabber#registerAction(Identifier, Codec)
 */
@FunctionalInterface
public interface DialogueActionV2 {
    /**
     * Handles a dialogue action triggered by the given player.
     *
     * @param player the player executing the action
     * @param interlocutor the entity with which the player is conversing, if any
     * @see org.ladysnake.blabber.Blabber#startDialogue(ServerPlayerEntity, Identifier, Entity)
     */
    void handle(ServerPlayerEntity player, @Nullable Entity interlocutor);
}
