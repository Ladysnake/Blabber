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

import io.github.ladysnake.blabber.Blabber;
import io.github.ladysnake.blabber.DialogueAction;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public final class DialogueRegistryImpl {
    private final Map<Identifier, DialogueAction> actions = new HashMap<>();

    public static final DialogueRegistryImpl INSTANCE = new DialogueRegistryImpl();

    public DialogueStateMachine startDialogue(World world, Identifier id) {
        return new DialogueStateMachine(
            world.getRegistryManager().get(BlabberRegistrar.DIALOGUE_REGISTRY).getOrEmpty(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown dialogue " + id)),
            id
        );
    }

    public void registerAction(Identifier actionId, DialogueAction action) {
        this.actions.put(actionId, action);
    }

    public DialogueAction getAction(Identifier actionId) {
        if (!this.actions.containsKey(actionId)) {
            Blabber.LOGGER.warn("[Blabber] Unknown dialogue action {}", actionId);
            return DialogueAction.NONE;
        }
        return this.actions.get(actionId);
    }
}
