/*
 * Blabber
 * Copyright (C) 2022-2026 Ladysnake
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
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.impl.common.model.DialogueTemplate;
import org.ladysnake.blabber.impl.common.packets.DialogueListPayload;
import org.ladysnake.blabber.impl.common.validation.DialogueLoadingException;
import org.ladysnake.blabber.impl.common.validation.DialogueValidator;
import org.ladysnake.blabber.impl.common.validation.ValidationResult;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public final class DialogueLoader extends SimpleReloadListener<Map<Identifier, JsonObject>> implements ServerLifecycleEvents.EndDataPackReload {
    public static final String BLABBER_DIALOGUES_PATH = "blabber/dialogues";
    public static final Identifier ID = Blabber.id("dialogue_loader");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        DialogueLoader instance = new DialogueLoader();
        ResourceLoader resourceLoader = ResourceLoader.get(PackType.SERVER_DATA);
        resourceLoader.registerReloadListener(ID, instance);
        resourceLoader.addListenerOrdering(ResourceReloaderKeys.Server.FUNCTIONS, ID);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(instance);
    }

    @Override
    protected Map<Identifier, JsonObject> prepare(SharedState sharedState) {
        Map<Identifier, JsonObject> data = new LinkedHashMap<>();
        sharedState.resourceManager().listResources(BLABBER_DIALOGUES_PATH, (res) -> res.getPath().endsWith(".json")).forEach((location, resource) -> {
            try (Reader in = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                JsonObject jsonObject = GSON.fromJson(in, JsonObject.class);
                data.put(location, jsonObject);
            } catch (IOException | JsonParseException e) {
                Blabber.LOGGER.error("(Blabber) Could not read dialogue file from {}", location, e);
                throw new DialogueLoadingException("Could not read dialogue file from " + location, e);
            }
        });
        return data;
    }

    @Override
    protected void apply(Map<Identifier, JsonObject> data, SharedState sharedState) {
        Map<Identifier, DialogueTemplate> dialogues = new LinkedHashMap<>();
        data.forEach((location, jsonObject) -> {
            Identifier id = Identifier.fromNamespaceAndPath(location.getNamespace(), location.getPath().substring(BLABBER_DIALOGUES_PATH.length() + 1, location.getPath().length() - 5));
            DialogueTemplate dialogue = DialogueTemplate.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow(message -> {
                Blabber.LOGGER.error("(Blabber) Could not parse dialogue file from {}: {}", location, message);
                return new RuntimeException(message);
            });
            switch (DialogueValidator.validateStructure(dialogue)) {
                case ValidationResult.Error error -> {
                    Blabber.LOGGER.error("(Blabber) Could not validate dialogue {}: {}", id, error.message());
                    throw new DialogueLoadingException("Could not validate dialogue file from " + location);
                }
                case ValidationResult.Warnings warnings -> {
                    Blabber.LOGGER.warn("(Blabber) Dialogue {} had warnings: {}", id, warnings.message());
                    dialogues.put(id, dialogue);
                }
                case ValidationResult.Success ignored -> {
                    dialogues.put(id, dialogue);
                }
            }
        });
        DialogueRegistry.setEntries(dialogues);
    }

    @Override
    public void endDataPackReload(MinecraftServer server, CloseableResourceManager resourceManager, boolean success) {
        if (success) {
            Set<Identifier> dialogueIds = DialogueRegistry.getIds();
            DialogueListPayload idSyncPacket = new DialogueListPayload(dialogueIds);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, idSyncPacket);
                PlayerDialogueTracker.get(player).updateDialogue();
            }
        }
    }

    private DialogueLoader() {}

}
