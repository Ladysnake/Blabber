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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.impl.common.model.ChoiceResult;
import org.ladysnake.blabber.impl.common.model.DialogueState;
import org.ladysnake.blabber.impl.common.model.DialogueTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public final class DialogueLoader implements SimpleResourceReloadListener<Map<Identifier, DialogueTemplate>>, ServerLifecycleEvents.EndDataPackReload {
    public static final String BLABBER_DIALOGUES_PATH = "blabber/dialogues";
    public static final Identifier ID = Blabber.id("dialogue_loader");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        DialogueLoader instance = new DialogueLoader();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(instance);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(instance);
    }

    @Override
    public CompletableFuture<Map<Identifier, DialogueTemplate>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, DialogueTemplate> data = new HashMap<>();
            manager.findResources(BLABBER_DIALOGUES_PATH, (res) -> res.getPath().endsWith(".json")).forEach((location, resource) -> {
                try (Reader in = new InputStreamReader(resource.getInputStream())) {
                    JsonObject jsonObject = GSON.fromJson(in, JsonObject.class);
                    Identifier id = new Identifier(location.getNamespace(), location.getPath().substring(BLABBER_DIALOGUES_PATH.length() + 1));
                    DialogueTemplate dialogue = DialogueTemplate.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow(false, message -> Blabber.LOGGER.error("(Blabber) Could not parse dialogue file from {}: {}", location, message));

                    if (validateStructure(id, dialogue)) {
                        data.put(id, dialogue);
                    }
                } catch (IOException | JsonParseException e) {
                    Blabber.LOGGER.error("(Blabber) Could not read dialogue file from {}", location, e);
                    throw new IllegalStateException(e);
                }
            });
            return data;
        }, executor);
    }

    private static boolean validateStructure(Identifier id, DialogueTemplate dialogue) {
        Map<String, Map<String, Reachability>> parents = new HashMap<>();
        Deque<String> waitList = new ArrayDeque<>();
        Map<String, Reachability> unvalidated = new HashMap<>();

        for (Map.Entry<String, DialogueState> state : dialogue.states().entrySet()) {
            if (state.getValue().type().equals(ChoiceResult.END_DIALOGUE)) {
                waitList.add(state.getKey());
            } else if (dialogue.states().get(state.getKey()).choices().isEmpty()) {
                Blabber.LOGGER.error("(Blabber) {}#{} has no available choices but is not an end state", id, state.getKey());
                return false;
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
                Blabber.LOGGER.warn("(Blabber) {}#{} is unreachable", id, bad.getKey());
            } else if (bad.getValue() == Reachability.CONDITIONAL) {
                Blabber.LOGGER.warn("(Blabber) {}#{} only has conditional paths to the end of the dialogue", id, bad.getKey());
            } else {
                Blabber.LOGGER.error("(Blabber) {}#{} does not have any path to the end of the dialogue", id, bad.getKey());
                return false;
            }
        }

        return true;
    }

    @Override
    public CompletableFuture<Void> apply(Map<Identifier, DialogueTemplate> data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> DialogueRegistry.setEntries(data), executor);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return Set.of(ResourceReloadListenerKeys.LOOT_TABLES);  // for dialogue choice predicates
    }

    @Override
    public void endDataPackReload(MinecraftServer server, LifecycledResourceManager resourceManager, boolean success) {
        if (success) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerDialogueTracker.get(player).updateDialogue();
            }
        }
    }

    private DialogueLoader() {}

    private enum Reachability {
        NONE,
        CONDITIONAL,
        PROVEN,
    }
}
