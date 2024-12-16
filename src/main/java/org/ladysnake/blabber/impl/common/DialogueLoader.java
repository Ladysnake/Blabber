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
package org.ladysnake.blabber.impl.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceType;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.IProfiler;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.impl.common.model.DialogueTemplate;
import org.ladysnake.blabber.impl.common.packets.DialogueListPacket;
import org.ladysnake.blabber.impl.common.validation.DialogueLoadingException;
import org.ladysnake.blabber.impl.common.validation.DialogueValidator;
import org.ladysnake.blabber.impl.common.validation.ValidationResult;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod.EventBusSubscriber(modid = Blabber.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DialogueLoader {
    public static final String BLABBER_DIALOGUES_PATH = "blabber/dialogues";
    public static final Identifier ID = Blabber.id("dialogue_loader");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new DialogueLoader());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        MinecraftServer server = event.getPlayerList().getServer();
        Set<Identifier> dialogueIds = DialogueRegistry.getIds();
        DialogueListPacket idSyncPacket = new DialogueListPacket(dialogueIds);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(idSyncPacket);
            PlayerDialogueTracker.get(player).updateDialogue();
        }
    }

    public CompletableFuture<Map<Identifier, DialogueTemplate>> load(IResourceManager manager, IProfiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, DialogueTemplate> data = new HashMap<>();
            manager.listResources(BLABBER_DIALOGUES_PATH, (res) -> res.getPath().endsWith(".json")).forEach((location, resource) -> {
                try (Reader in = new InputStreamReader(resource.getInputStream())) {
                    JsonObject jsonObject = GSON.fromJson(in, JsonObject.class);
                    Identifier id = new Identifier(location.getNamespace(), location.getPath().substring(BLABBER_DIALOGUES_PATH.length() + 1, location.getPath().length() - 5));
                    DialogueTemplate dialogue = DialogueTemplate.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow(false, message -> Blabber.LOGGER.error("(Blabber) Could not parse dialogue file from {}: {}", location, message));
                    ValidationResult result = DialogueValidator.validateStructure(dialogue);
                    if (result instanceof ValidationResult.Error error) {
                        Blabber.LOGGER.error("(Blabber) Could not validate dialogue {}: {}", id, error.message());
                        throw new DialogueLoadingException("Could not validate dialogue file from " + location);
                    } else if (result instanceof ValidationResult.Warnings warnings) {
                        Blabber.LOGGER.warn("(Blabber) Dialogue {} had warnings: {}", id, warnings.message());
                    }

                    data.put(id, dialogue);
                } catch (IOException | JsonParseException e) {
                    Blabber.LOGGER.error("(Blabber) Could not read dialogue file from {}", location, e);
                    throw new DialogueLoadingException("Could not read dialogue file from " + location, e);
                }
            });
            return data;
        }, executor);
    }

    public CompletableFuture<Void> apply(Map<Identifier, DialogueTemplate> data, IResourceManager manager, IProfiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> DialogueRegistry.setEntries(data), executor);
    }

    private DialogueLoader() {}
}
