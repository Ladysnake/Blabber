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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.ladysnake.blabber.Blabber;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    private static final Gson GSON = new Gson();

    public static final Codec<DialogueTemplate> NETWORK_CODEC = codec(Codec.STRING.xmap(
            str -> GSON.fromJson(str, JsonElement.class),
            GSON::toJson
    ));

    public static final Codec<DialogueTemplate> CODEC = codec(Codec.PASSTHROUGH.comapFlatMap(
            dynamic -> DataResult.success(dynamic.convert(JsonOps.INSTANCE).getValue()),
            json -> new Dynamic<>(JsonOps.INSTANCE, json)
    ));

    private final String start;
    private final boolean unskippable;
    private final Map<String, DialogueState> states;

    private DialogueTemplate(String start, boolean unskippable, Map<String, DialogueState> states) {
        this.start = start;
        this.unskippable = unskippable;
        this.states = Map.copyOf(states);
    }

    private static Codec<DialogueTemplate> codec(Codec<JsonElement> jsonCodec) {
        return RecordCodecBuilder.<DialogueTemplate>create(instance -> instance.group(
            Codec.STRING.fieldOf("start_at").forGetter(DialogueTemplate::start),
            Codec.BOOL.optionalFieldOf("unskippable", false).forGetter(DialogueTemplate::unskippable),
            Codec.unboundedMap(Codec.STRING, DialogueState.codec(jsonCodec)).fieldOf("states").forGetter(DialogueTemplate::states)
        ).apply(instance, DialogueTemplate::new)).mapResult(new Codec.ResultFunction<>() {
            @Override
            public <T> DataResult<Pair<DialogueTemplate, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<DialogueTemplate, T>> a) {
                return a.flatMap(p -> validateStructure(p.getFirst()).map(t -> Pair.of(t, p.getSecond())));
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> ops, DialogueTemplate input, DataResult<T> t) {
                return t;
            }
        });
    }

    private static DataResult<DialogueTemplate> validateStructure(DialogueTemplate dialogue) {
        Map<String, Set<String>> ancestors = new HashMap<>();
        Deque<String> waitList = new ArrayDeque<>();
        Set<String> unvalidated = new HashSet<>();

        for (Map.Entry<String, DialogueState> state : dialogue.states().entrySet()) {
            if (state.getValue().type().equals(ChoiceResult.END_DIALOGUE)) {
                waitList.add(state.getKey());
            } else if (dialogue.states().get(state.getKey()).choices().isEmpty()) {
                return DataResult.error("(Blabber) %s has no available choices but is not an end state".formatted(state.getKey()));
            } else {
                unvalidated.add(state.getKey());
                for (DialogueState.Choice choice : state.getValue().choices()) {
                    ancestors.computeIfAbsent(choice.next(), n -> new HashSet<>()).add(state.getKey());
                }
            }
        }

        while (!waitList.isEmpty()) {
            String state = waitList.pop();

            if (ancestors.containsKey(state)) {
                for (var ancestor : ancestors.get(state)) {
                    if (unvalidated.remove(ancestor)) {
                        waitList.add(ancestor);
                    }
                }
            } else if (!state.equals(dialogue.start())) {
                Blabber.LOGGER.warn("{} is unreachable", state);
            }
        }

        for (String bad : unvalidated) {
            if (!Objects.equals(bad, dialogue.start()) && !ancestors.containsKey(bad)) {
                // Unreachable states do not cause infinite loops, but we still want to be aware of them
                Blabber.LOGGER.warn("{} is unreachable", bad);
            } else {
                return DataResult.error("(Blabber) %s does not have any path to the end of the dialogue".formatted(bad));
            }
        }

        return DataResult.success(dialogue, Lifecycle.stable());
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

    @Override
    public String toString() {
        return "DialogueTemplate[start=%s, states=%s%s]".formatted(start, states, unskippable ? " (unskippable)" : "");
    }

}
