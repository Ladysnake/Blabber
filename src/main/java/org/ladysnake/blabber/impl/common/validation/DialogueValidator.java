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
package org.ladysnake.blabber.impl.common.validation;

import org.ladysnake.blabber.impl.common.model.ChoiceResult;
import org.ladysnake.blabber.impl.common.model.DialogueChoice;
import org.ladysnake.blabber.impl.common.model.DialogueState;
import org.ladysnake.blabber.impl.common.model.DialogueTemplate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DialogueValidator {
    public static ValidationResult validateStructure(DialogueTemplate dialogue) {
        Map<String, Map<String, Reachability>> parents = new HashMap<>();
        Deque<String> waitList = new ArrayDeque<>();
        Map<String, Reachability> unvalidated = new HashMap<>();
        List<ValidationResult.Warning> warnings = new ArrayList<>();

        for (Map.Entry<String, DialogueState> state : dialogue.states().entrySet()) {
            if (state.getValue().type().equals(ChoiceResult.END_DIALOGUE)) {
                waitList.add(state.getKey());
            } else if (dialogue.states().get(state.getKey()).choices().isEmpty()) {
                return new ValidationResult.Error.NoChoice(state.getKey());
            } else {
                unvalidated.put(state.getKey(), Reachability.NONE);

                for (DialogueChoice choice : state.getValue().choices()) {
                    parents.computeIfAbsent(choice.next(), n -> new HashMap<>()).put(
                            state.getKey(),
                            choice.condition().isPresent() ? Reachability.CONDITIONAL : Reachability.PROVEN
                    );
                }
            }
        }

        while (!waitList.isEmpty()) {
            String state = waitList.pop();
            Map<String, Reachability> stateParents = parents.get(state);

            if (stateParents != null) {
                for (var parent : stateParents.entrySet()) {
                    Reachability reachability = unvalidated.get(parent.getKey());

                    if (reachability != null) { // leave it alone if it was already validated through another branch
                        if (reachability == Reachability.NONE) {    // only check once
                            waitList.add(parent.getKey());
                        }

                        switch (parent.getValue()) {
                            case PROVEN -> unvalidated.remove(parent.getKey());
                            case CONDITIONAL -> unvalidated.put(parent.getKey(), Reachability.CONDITIONAL);
                            default -> throw new IllegalStateException("Unexpected parent-child reachability " + parent.getValue());
                        }
                    }
                }
            }   // else, state is unreachable - we log that in the next part
        }

        for (var bad : unvalidated.entrySet()) {
            if (!Objects.equals(bad.getKey(), dialogue.start()) && !parents.containsKey(bad.getKey())) {
                // Unreachable states do not cause infinite loops, but we still want to be aware of them
                warnings.add(new ValidationResult.Warning.Unreachable(bad.getKey()));
            } else if (bad.getValue() == Reachability.CONDITIONAL) {
                warnings.add(new ValidationResult.Warning.ConditionalSoftLock(bad.getKey()));
            } else {
                return new ValidationResult.Error.SoftLock(bad.getKey());
            }
        }

        return warnings.isEmpty() ? ValidationResult.success() : new ValidationResult.Warnings(warnings);
    }

    private enum Reachability {
        NONE,
        CONDITIONAL,
        PROVEN,
    }
}
