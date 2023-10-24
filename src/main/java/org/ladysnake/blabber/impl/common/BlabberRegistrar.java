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

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.ladysnake.blabber.Blabber;
import org.ladysnake.blabber.DialogueAction;
import org.ladysnake.blabber.impl.common.machine.DialogueStateMachine;
import org.ladysnake.blabber.impl.common.model.DialogueTemplate;
import org.ladysnake.blabber.impl.common.packets.SelectedDialogueStatePacket;

public final class BlabberRegistrar implements EntityComponentInitializer {
    public static final ScreenHandlerType<DialogueScreenHandler> DIALOGUE_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, Blabber.id("dialogue"), new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> {
        DialogueStateMachine dialogue = DialogueStateMachine.fromPacket(inventory.player.getWorld(), buf);
        return new DialogueScreenHandler(syncId, dialogue);
    }));
    public static final Identifier DIALOGUE_ACTION = Blabber.id("dialogue_action");
    public static final RegistryKey<Registry<DialogueTemplate>> DIALOGUE_REGISTRY_KEY = RegistryKey.ofRegistry(Blabber.id("dialogues"));
    public static final RegistryKey<Registry<Codec<? extends DialogueAction>>> ACTION_REGISTRY_KEY = RegistryKey.ofRegistry(Blabber.id("dialogue_actions"));
    public static final Registry<Codec<? extends DialogueAction>> ACTION_REGISTRY = FabricRegistryBuilder.from(
            new SimpleRegistry<>(ACTION_REGISTRY_KEY, Lifecycle.stable(), false)
    ).buildAndRegister();
    public static final SuggestionProvider<ServerCommandSource> ALL_DIALOGUES = SuggestionProviders.register(Blabber.id("available_dialogues"), (context, builder) -> CommandSource.suggestIdentifiers(context.getSource().getRegistryManager().get(DIALOGUE_REGISTRY_KEY).getIds(), builder));

    public static void init() {
        DynamicRegistries.registerSynced(DIALOGUE_REGISTRY_KEY, DialogueTemplate.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DIALOGUE_ACTION, (server, player, handler, buf, responseSender) -> {
            int choice = buf.readByte();
            server.execute(() -> {
                if (player.currentScreenHandler instanceof DialogueScreenHandler dialogueHandler) {
                    if (!dialogueHandler.makeChoice(player, choice)) {
                        ServerPlayNetworking.send(player, new SelectedDialogueStatePacket(dialogueHandler.getCurrentStateKey()));
                    }
                }
            });
        });
    }

    public static DialogueStateMachine startDialogue(World world, Identifier id) {
        return new DialogueStateMachine(
            world.getRegistryManager().get(DIALOGUE_REGISTRY_KEY).getOrEmpty(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown dialogue " + id)),
            id
        );
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PlayerDialogueTracker.KEY, PlayerDialogueTracker::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
