/*
 * Blabber
 * Copyright (C) 2022-2023 Ladysnake
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

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.ladysnake.blabber.impl.common.model.DialogueTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/*
 This could have been a dynamic registry, and it was at some point. However, since
 1) the dialogues are now synced one at a time during play rather than at configuration,
 2) dynamic registries can only be reloaded by restarting the server,
 3) codecs give terrible error messages,
 and 4) codecs are inefficient in networking,
 going back to old-time stuff seemed best.
 (still using codecs to a degree, but validation is done separately now)
 */

public final class DialogueRegistry {
    private static Set<Identifier> clientDialogueIds = Set.of();
    private static Map<Identifier, DialogueTemplate> entries = Map.of();

    public static Set<Identifier> getClientIds() {
        return clientDialogueIds;
    }

    public static Set<Identifier> getIds() {
        return entries.keySet();
    }

    public static Optional<DialogueTemplate> getOrEmpty(Identifier id) {
        return Optional.ofNullable(entries.get(id));
    }

    public static boolean containsId(Identifier id) {
        return getIds().contains(id);
    }

    static void setEntries(Map<Identifier, DialogueTemplate> newEntries) {
        entries = newEntries;
    }

    @ApiStatus.Internal // highly internal
    public static void setClientIds(Set<Identifier> dialogueIds) {
        clientDialogueIds = dialogueIds;
    }
}
