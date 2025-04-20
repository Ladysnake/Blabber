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
package org.ladysnake.blabber.api.illustration;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * An illustration that can be rendered in dialogues, such as entities or items.
 *
 * @apiNote this API is still being tweaked, breaking changes may occur without a major release
 */
@ApiStatus.Experimental
public interface DialogueIllustration {
    /**
     * @return the DialogueIllustrationType that corresponds to this illustration
     */
    DialogueIllustrationType<? extends DialogueIllustration> getType();

    /**
     * If this illustration contains some text, this will be parsed *server-side*.
     * @param source the context in which this is parsed
     * @param sender the player that is going to dialogue
     * @return a DialogueIllustration with the required text parsed (often, simply this)
     */
    default DialogueIllustration parseText(@Nullable ServerCommandSource source, @Nullable Entity sender) throws CommandSyntaxException {
        return this;
    }
}
