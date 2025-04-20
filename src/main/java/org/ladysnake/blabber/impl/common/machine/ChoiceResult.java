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
package org.ladysnake.blabber.impl.common.machine;

import org.ladysnake.blabber.impl.common.InstancedDialogueAction;
import org.ladysnake.blabber.impl.common.model.StateType;

import java.util.Optional;

public interface ChoiceResult {
    StateType type();
    Optional<InstancedDialogueAction<?>> action();

    ChoiceResult DEFAULT_END = new Basic(StateType.END_DIALOGUE, Optional.empty());

    record Basic(StateType type, Optional<InstancedDialogueAction<?>> action) implements ChoiceResult {}
}
