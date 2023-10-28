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
package org.ladysnake.blabber.impl.common.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;
import org.ladysnake.blabber.Blabber;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DialogueTemplate {
    // SO
    // Mojang just decided to use the identity hash strategy for SimpleRegistry#entryToRawId
    // but not for anything else
    // and the fastutil maps do not update a mapping if Objects.equals(oldValue, newValue)
    // and dynamic registries use Registry#replace every time they are reloaded
    // so with a proper equals and hashcode implementation, we end up with a stupid identity mismatch
    // and this identity mismatch snowballs into an error if a third reload happens (which always happens with datapacks on)
    // this was hell to debug and I hate mojang but here we are
    // so what does all this mean ? It means no using record instead of class lol (or having to break Record's contract)

    public static final Codec<DialogueTemplate> CODEC = Codecs.validate(RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("start_at").forGetter(DialogueTemplate::start),
            Codec.BOOL.optionalFieldOf("unskippable", false).forGetter(DialogueTemplate::unskippable),
            Codec.unboundedMap(Codec.STRING, DialogueState.CODEC).fieldOf("states").forGetter(DialogueTemplate::states),
            DialogueLayout.CODEC.optionalFieldOf("layout", DialogueLayout.DEFAULT).forGetter(DialogueTemplate::layout)
    ).apply(instance, DialogueTemplate::new)), DialogueTemplate::validateStructure);

    private static DataResult<DialogueTemplate> validateStructure(DialogueTemplate dialogue) {
        Map<String, Map<String, Reachability>> parents = new HashMap<>();
        Deque<String> waitList = new ArrayDeque<>();
        Map<String, Reachability> unvalidated = new HashMap<>();

        for (Map.Entry<String, DialogueState> state : dialogue.states().entrySet()) {
            if (state.getValue().type().equals(ChoiceResult.END_DIALOGUE)) {
                waitList.add(state.getKey());
            } else if (dialogue.states().get(state.getKey()).choices().isEmpty()) {
                return DataResult.error(() -> "(Blabber) %s has no available choices but is not an end state".formatted(state.getKey()));
            } else {
                unvalidated.put(state.getKey(), Reachability.NONE);

                for (DialogueState.Choice choice : state.getValue().choices()) {
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
                Blabber.LOGGER.warn("{} is unreachable", bad.getKey());
            } else if (bad.getValue() == Reachability.CONDITIONAL) {
                Blabber.LOGGER.warn("(Blabber) {} only has conditional paths to the end of the dialogue", bad.getKey());
            } else {
                return DataResult.error(() -> "(Blabber) %s does not have any path to the end of the dialogue".formatted(bad.getKey()));
            }
        }

        return DataResult.success(dialogue, Lifecycle.stable());
    }

    private final String start;
    private final boolean unskippable;
    private final Map<String, DialogueState> states;
    private final DialogueLayout layout;

    private DialogueTemplate(String start, boolean unskippable, Map<String, DialogueState> states, DialogueLayout layout) {
        this.start = start;
        this.unskippable = unskippable;
        this.states = Map.copyOf(states);
        this.layout = layout;
    }

    public boolean unskippable() {
        return this.unskippable;
    }

    public String start() {
        return start;
    }

    public Map<String, DialogueState> states() {
        return states;
    }

    public DialogueLayout layout() {
        return this.layout;
    }

    @Override
    public String toString() {
        return "DialogueTemplate[start=%s, states=%s%s]".formatted(start, states, unskippable ? " (unskippable)" : "");
    }

    private enum Reachability {
        NONE,
        CONDITIONAL,
        PROVEN,
    }
}
